package com.sms.billing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions", indexes = @Index(name = "idx_subscription_org", columnList = "organizationId"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Denormalized from Customer so this table is independently queryable/indexable by tenant. */
    @Column(nullable = false)
    private String organizationId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * References a plan owned by plan-catalog-service - NOT a local entity.
     * When this Subscription is created from a subscription.created Kafka
     * event (see SubscriptionEventListener), the event carries
     * productCode/planCode/planVersion rather than a single opaque id, so
     * the listener encodes them into this field as "productCode:planCode:vN"
     * rather than leaving it unset - it stays non-null and recoverable
     * either way.
     */
    @Column(nullable = false)
    private String planId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING_ACTIVATION;

    /**
     * Correlates to subscription-service's own Subscription.id (a Long
     * there, per that entity's IDENTITY strategy) when this row was created
     * from a subscription.created event. Null if created directly via
     * POST /api/v1/billing/subscriptions instead, if that entry point is
     * still used alongside the event-driven flow.
     */
    private String masterSubscriptionId;

    private String razorpaySubscriptionId;

    private LocalDate currentPeriodStart;

    private LocalDate currentPeriodEnd;

    private LocalDate nextBillingDate;

    private LocalDate cancelledAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Status {
        PENDING_ACTIVATION, ACTIVE, PAST_DUE, PAUSED, CANCELLED, EXPIRED
    }

    /**
     * WEEKLY added to match subscription-service's BillingCycle
     * (WEEKLY, MONTHLY, QUARTERLY, ANNUAL) - confirmed from that service's
     * actual domain/BillingCycle.java, not assumed. Note subscription-service
     * uses "ANNUAL" where this enum uses "YEARLY" - SubscriptionEventListener
     * maps between the two explicitly; they are NOT the same string.
     */
    public enum BillingCycle {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    }
}
