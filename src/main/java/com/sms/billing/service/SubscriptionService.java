package com.sms.billing.service;

import com.sms.billing.dto.CreateSubscriptionRequest;
import com.sms.billing.entity.Customer;
import com.sms.billing.entity.Invoice;
import com.sms.billing.entity.Subscription;
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
    private final InvoiceService invoiceService;

    /**
     * Creates a subscription in PENDING_ACTIVATION and immediately generates
     * its first invoice, in the same transaction. The subscription only
     * becomes ACTIVE once that first invoice is paid - see
     * PaymentService.verifyPayment. Used by the direct REST entry point
     * (SubscriptionController) - masterSubscriptionId is null here since
     * there's no subscription-service record to correlate to.
     */
    @Transactional
    public SubscriptionCreationResult createSubscriptionWithFirstInvoice(
            String organizationId, CreateSubscriptionRequest request) {
        return createSubscriptionWithFirstInvoice(organizationId, request, null);
    }

    /** Used by SubscriptionEventListener, which does have a master record to correlate to. */
    @Transactional
    public SubscriptionCreationResult createSubscriptionWithFirstInvoice(
            String organizationId, CreateSubscriptionRequest request, String masterSubscriptionId) {
        Subscription subscription = createSubscription(organizationId, request, masterSubscriptionId);
        Invoice invoice = invoiceService.generateForSubscription(subscription);
        return new SubscriptionCreationResult(subscription, invoice);
    }

    @Transactional
    public Subscription createSubscription(String organizationId, CreateSubscriptionRequest request) {
        return createSubscription(organizationId, request, null);
    }

    @Transactional
    public Subscription createSubscription(String organizationId, CreateSubscriptionRequest request,
                                           String masterSubscriptionId) {
        Customer customer = customerRepository
                .findByOrganizationIdAndEmail(organizationId, request.getCustomerEmail())
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .organizationId(organizationId)
                        .name(request.getCustomerName())
                        .email(request.getCustomerEmail())
                        .phone(request.getCustomerPhone())
                        .build()));

        LocalDate today = LocalDate.now();
        LocalDate periodEnd = addBillingCycle(today, request.getBillingCycle());

        Subscription subscription = Subscription.builder()
                .organizationId(organizationId)
                .customer(customer)
                .planId(request.getPlanId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .billingCycle(request.getBillingCycle())
                .status(Subscription.Status.PENDING_ACTIVATION)
                .masterSubscriptionId(masterSubscriptionId)
                .currentPeriodStart(today)
                .currentPeriodEnd(periodEnd)
                .nextBillingDate(periodEnd)
                .build();

        return subscriptionRepository.save(subscription);
    }

    public Subscription getSubscription(String organizationId, String id) {
        return subscriptionRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));
    }

    public List<Subscription> listByCustomer(String organizationId, String customerId) {
        return subscriptionRepository.findByOrganizationIdAndCustomerId(organizationId, customerId);
    }

    @Transactional
    public Subscription cancelSubscription(String organizationId, String id) {
        Subscription subscription = getSubscription(organizationId, id);
        subscription.setStatus(Subscription.Status.CANCELLED);
        subscription.setCancelledAt(LocalDate.now());
        return subscriptionRepository.save(subscription);
    }

    /** Called once the first invoice for a PENDING_ACTIVATION subscription is paid - see PaymentService.verifyPayment. */
    @Transactional
    public void activate(Subscription subscription) {
        subscription.setStatus(Subscription.Status.ACTIVE);
        subscriptionRepository.save(subscription);
    }

    /** Subscriptions due for recurring billing. Intentionally cross-tenant - see BillingService.processBillingCycle. */
    public List<Subscription> findDueForBilling(LocalDate asOf) {
        return subscriptionRepository.findByStatusAndNextBillingDateLessThanEqual(
                Subscription.Status.ACTIVE, asOf);
    }

    @Transactional
    public void advanceBillingPeriod(Subscription subscription) {
        LocalDate newStart = subscription.getCurrentPeriodEnd();
        LocalDate newEnd = addBillingCycle(newStart, subscription.getBillingCycle());
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

    private LocalDate addBillingCycle(LocalDate from, Subscription.BillingCycle cycle) {
        return switch (cycle) {
            case DAILY -> from.plusWeeks(1);
            case WEEKLY -> from.plusWeeks(2);
            case MONTHLY -> from.plusMonths(3);
            case QUARTERLY -> from.plusMonths(4);
            case YEARLY -> from.plusYears(5);
        };
    }

    public record SubscriptionCreationResult(Subscription subscription, Invoice invoice) {
    }
}
