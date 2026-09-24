package com.sms.billing.dto;

/**
 * Response for POST /api/v1/billing/subscriptions specifically - wraps the
 * subscription (PENDING_ACTIVATION at this point) together with the
 * invoiceId/invoice the caller needs to pay next, via
 * POST /api/v1/billing/payments/orders?invoiceId=... to get a Razorpay
 * order, then POST /api/v1/billing/payments/verify after checkout completes.
 */
public record SubscriptionCreatedResponse(
        SubscriptionResponse subscription,
        InvoiceResponse invoice
) {
}
