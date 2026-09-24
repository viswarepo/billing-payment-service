package com.sms.billing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Matches the shape of Razorpay's client-side payment.failed event's
 * response.error object exactly (confirmed against Razorpay's current
 * error-structure docs): { code, description, field, source, step, reason,
 * metadata: { order_id, payment_id } }.
 *
 * The frontend's payment.failed handler POSTs this here as soon as
 * Checkout reports a failure - previously nothing did, so a failed payment
 * left its Payment row stuck at CREATED forever with no reason recorded.
 */
@Data
public class PaymentFailureRequest {

    @NotBlank
    private String razorpayOrderId;

    /** May be null - Razorpay doesn't always have a payment_id yet when failure occurs early (e.g. before a payment attempt is even created). */
    private String razorpayPaymentId;

    private String code;
    private String description;
    private String source;
    private String step;
    private String reason;
}
