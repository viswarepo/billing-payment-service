package com.sms.billing.controller;

import com.sms.billing.dto.CreateSubscriptionRequest;
import com.sms.billing.dto.SubscriptionResponse;
import com.sms.billing.entity.Subscription;
import com.sms.billing.service.BillingService;
import com.sms.billing.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final BillingService billingService;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody CreateSubscriptionRequest request) {
        Subscription subscription = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SubscriptionResponse.from(subscription));
    }

    @GetMapping("/{id}")
    public SubscriptionResponse get(@PathVariable Long id) {
        return SubscriptionResponse.from(subscriptionService.getSubscription(id));
    }

    @GetMapping
    public List<SubscriptionResponse> listByCustomer(@RequestParam Long customerId) {
        return subscriptionService.listByCustomer(customerId).stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    @PostMapping("/{id}/cancel")
    public SubscriptionResponse cancel(@PathVariable Long id) {
        return SubscriptionResponse.from(subscriptionService.cancelSubscription(id));
    }

    /**
     * Manually triggers the recurring billing cycle (also runs on the daily
     * schedule). Restricted to ADMIN by the gateway's AuthorizationGlobalFilter.
     */
    @PostMapping("/process-billing-cycle")
    public BillingService.BillingCycleResult processBillingCycle() {
        return billingService.processBillingCycle();
    }
}
