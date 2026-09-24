package com.sms.billing.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Plan Creation Event
 */
public record PlanCreatedEvent(
        UUID eventId,
        String planId,
        String organizationId,
        String productId,
        String name,
        String currency,
        String billingCycle,
        BigDecimal unitAmount,
        Date occurredAt
) {

}
