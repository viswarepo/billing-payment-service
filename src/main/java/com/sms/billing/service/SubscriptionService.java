package com.sms.billing.service;

import com.sms.billing.dto.CreateSubscriptionRequest;
import com.sms.billing.entity.Customer;
import com.sms.billing.entity.Plan;
import com.sms.billing.entity.Subscription;
import com.sms.billing.exception.InvalidRequestException;
import com.sms.billing.exception.ResourceNotFoundException;
import com.sms.billing.repository.CustomerRepository;
import com.sms.billing.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final PlanService planService;

    @Transactional
    public Subscription createSubscription(CreateSubscriptionRequest request) {
        Plan plan = planService.getPlan(request.getPlanId());
        if (!plan.isActive()) {
            throw new InvalidRequestException("Plan is not active: " + plan.getId());
        }

        Customer customer = customerRepository.findByEmail(request.getCustomerEmail())
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .organizationId(request.getOrganizationId())
                        .name(request.getCustomerName())
                        .email(request.getCustomerEmail())
                        .phone(request.getCustomerPhone())
                        .build()));

        LocalDate today = LocalDate.now();
        LocalDate periodEnd = addBillingCycle(today, plan.getBillingCycle());

        Subscription subscription = Subscription.builder()
                .customer(customer)
                .plan(plan)
                .status(Subscription.Status.ACTIVE)
                .currentPeriodStart(today)
                .currentPeriodEnd(periodEnd)
                .nextBillingDate(periodEnd)
                .build();

        return subscriptionRepository.save(subscription);
    }

    public Subscription getSubscription(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));
    }

    public List<Subscription> listByCustomer(Long customerId) {
        return subscriptionRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Subscription cancelSubscription(Long id) {
        Subscription subscription = getSubscription(id);
        subscription.setStatus(Subscription.Status.CANCELLED);
        subscription.setCancelledAt(LocalDate.now());
        return subscriptionRepository.save(subscription);
    }

    /** Subscriptions whose next billing date has arrived and are still active. */
    public List<Subscription> findDueForBilling(LocalDate asOf) {
        return subscriptionRepository.findByStatusAndNextBillingDateLessThanEqual(
                Subscription.Status.ACTIVE, asOf);
    }

    @Transactional
    public void advanceBillingPeriod(Subscription subscription) {
        LocalDate newStart = subscription.getCurrentPeriodEnd();
        LocalDate newEnd = addBillingCycle(newStart, subscription.getPlan().getBillingCycle());
        subscription.setCurrentPeriodStart(newStart);
        subscription.setCurrentPeriodEnd(newEnd);
        subscription.setNextBillingDate(newEnd);
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void markPastDue(Subscription subscription) {
        subscription.setStatus(Subscription.Status.PAST_DUE);
        subscriptionRepository.save(subscription);
    }

    private LocalDate addBillingCycle(LocalDate from, Plan.BillingCycle cycle) {
        return switch (cycle) {
            case MONTHLY -> from.plusMonths(1);
            case QUARTERLY -> from.plusMonths(3);
            case YEARLY -> from.plusYears(1);
        };
    }
}
