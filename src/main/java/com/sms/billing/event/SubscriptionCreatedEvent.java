package com.sms.billing.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
public record SubscriptionCreatedEvent(
        UUID eventId,
        String subscriptionId,
        String organizationId,
        String customerId,
        String customerEmail,
        String customerName,
        String productCode,
        String planCode,
        int planVersion,
        BigDecimal unitAmount,
        String currency,
        String billingCycle,
        int trialDays,
        Date occurredAt
) {
}
