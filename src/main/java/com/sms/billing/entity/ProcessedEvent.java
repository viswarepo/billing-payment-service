package com.sms.billing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Records that a given Kafka event has already been handled, so a duplicate
 * delivery (Kafka's default guarantee is at-least-once, not exactly-once)
 * doesn't create a second Subscription for the same event.
 */
@Entity
@Table(name = "processed_events", uniqueConstraints = @UniqueConstraint(columnNames = "eventId"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private Instant processedAt;
}
