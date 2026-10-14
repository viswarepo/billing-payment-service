package com.sms.billing.event;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Local copy of the event published by subscription-service
 * (com.sms.sub.event.SubscriptionCreatedEvent) on topic "subscription.created".
 * Field names/types verified against that service's actual current source,
 * not assumed - subscriptionId and customerId are Long there (subscription-service's
 * Subscription/Customer entities use IDENTITY Long ids), so they stay Long
 * here too even though billing-service's own entities use String UUIDs.
 */
public record InvoicePaidEvent(
         String paymentId,
         String organizationId,
         BigDecimal amount,
         String mail,
         String mobile,
         String customerId,
         String customernme,
         String currency,
         String status,
         String razorpayOrderId,
         String razorpayPaymentId,
         String razorpaySignature,
         String paymentMethod,
         String failureReason,

         String invoiceId,
         String subscriptionId
) {
}
