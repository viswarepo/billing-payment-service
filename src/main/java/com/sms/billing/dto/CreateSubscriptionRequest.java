package com.sms.billing.dto;

import com.sms.billing.entity.Subscription;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSubscriptionRequest {

    @NotBlank
    private String customerName;

    @NotBlank
    @Email
    private String customerEmail;

    private String customerPhone;

    /** References a plan owned by plan-catalog-service - not a local entity. */
    @NotNull
    private String planId;

    /**
     * Snapshotted at creation time rather than looked up from planId live -
     * matches subscription-service's convention so a subscriber's price
     * can't silently change if the plan is re-priced later.
     */
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotNull
    private Subscription.BillingCycle billingCycle;

    // organizationId REMOVED from this DTO on purpose - it used to be a
    // plain client-supplied body field, which meant any caller could create
    // a subscription under any organization they chose. It now comes only
    // from the gateway-verified X-Organization-Id header - see
    // SubscriptionController.
}
