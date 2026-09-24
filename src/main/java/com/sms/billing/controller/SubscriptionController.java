package com.sms.billing.controller;

import com.sms.billing.dto.CreateSubscriptionRequest;
import com.sms.billing.dto.InvoiceResponse;
import com.sms.billing.dto.SubscriptionCreatedResponse;
import com.sms.billing.dto.SubscriptionResponse;
import com.sms.billing.service.BillingService;
import com.sms.billing.service.SubscriptionService;
import com.sms.billing.service.SubscriptionService.SubscriptionCreationResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billing/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final BillingService billingService;

    /**
     * Creates the subscription (PENDING_ACTIVATION) and its first invoice in
     * one call. The response's invoice.id is what you pass to
     * POST /payments/orders next - see SubscriptionCreatedResponse javadoc.
     */
    @PostMapping
    public ResponseEntity<SubscriptionCreatedResponse> create(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @Valid @RequestBody CreateSubscriptionRequest request) {
        SubscriptionCreationResult result =
                subscriptionService.createSubscriptionWithFirstInvoice(organizationId, request);
        SubscriptionCreatedResponse body = new SubscriptionCreatedResponse(
                SubscriptionResponse.from(result.subscription()),
                InvoiceResponse.from(result.invoice()));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{id}")
    public SubscriptionResponse get(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String id) {
        return SubscriptionResponse.from(subscriptionService.getSubscription(organizationId, id));
    }

    @GetMapping
    public List<SubscriptionResponse> listByCustomer(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @RequestParam String customerId) {
        return subscriptionService.listByCustomer(organizationId, customerId).stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    @PostMapping("/{id}/cancel")
    public SubscriptionResponse cancel(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String id) {
        return SubscriptionResponse.from(subscriptionService.cancelSubscription(organizationId, id));
    }

    /**
     * Manually triggers the recurring billing cycle (also runs on the daily
     * schedule), across ALL organizations. Restricted to ADMIN at the
     * gateway; not organization-scoped, same as subscription-service's
     * analogous endpoint.
     */
    @PostMapping("/process-billing-cycle")
    public BillingService.BillingCycleResult processBillingCycle() {
        return billingService.processBillingCycle();
    }
}
