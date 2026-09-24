package com.sms.billing.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.billing.dto.CreateSubscriptionRequest;
import com.sms.billing.entity.ProcessedEvent;
import com.sms.billing.entity.Subscription;
import com.sms.billing.event.SubscriptionCreatedEvent;
import com.sms.billing.repository.ProcessedEventRepository;
import com.sms.billing.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

/**
 * Consumes subscription.created events published by subscription-service
 * (the master record owner) and creates the corresponding local Subscription
 * + first Invoice in billing-payment-service - the event-driven counterpart
 * to the direct POST /api/v1/billing/subscriptions entry point.
 *
 * Kafka's default delivery guarantee is at-least-once, so this method must
 * tolerate the same event arriving more than once - see the idempotency
 * check against ProcessedEvent below.
 */
@Component
@Slf4j
public class SubscriptionEventListener {

    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;
    private final SubscriptionService subscriptionService;

    public SubscriptionEventListener(ObjectMapper objectMapper,
                                      ProcessedEventRepository processedEventRepository,
                                      SubscriptionService subscriptionService) {
        this.objectMapper = objectMapper;
        this.processedEventRepository = processedEventRepository;
        this.subscriptionService = subscriptionService;
    }

    @KafkaListener(topics = "subscription.created", groupId = "${spring.kafka.consumer.group-id:billing-payment-service}")
    @Transactional
    public void onSubscriptionCreated(String rawJson) {
        SubscriptionCreatedEvent event;
        try {
            event = objectMapper.readValue(rawJson, SubscriptionCreatedEvent.class);
        } catch (JsonProcessingException e) {
            // Malformed message - logging and dropping rather than retrying forever.
            // A dead-letter topic is the real fix for poison messages; not built
            // here - see the manifest's extension-points note.
            log.error("Could not parse SubscriptionCreatedEvent, dropping message: {}", rawJson, e);
            return;
        }

        String eventId = event.eventId().toString();
        if (processedEventRepository.existsByEventId(eventId)) {
            log.info("SubscriptionCreatedEvent {} already processed - skipping duplicate delivery", eventId);
            return;
        }

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerName(event.customerName());
        request.setCustomerEmail(event.customerEmail());
        // planId bridges subscription-service's productCode+planCode+planVersion
        // triple into billing-service's single required planId string - see
        // the field javadoc on Subscription.planId for why.
        request.setPlanId(event.productCode() + ":" + event.planCode() + ":v" + event.planVersion());
        request.setAmount(event.unitAmount());
        request.setCurrency(event.currency());
        request.setBillingCycle(parseBillingCycle(event.billingCycle()));

        SubscriptionService.SubscriptionCreationResult result =
                subscriptionService.createSubscriptionWithFirstInvoice(
                        event.organizationId(), request, event.subscriptionId());

        processedEventRepository.save(ProcessedEvent.builder()
                .eventId(eventId)
                .eventType("subscription.created")
                .processedAt(Instant.now())
                .build());

        log.info("Created billing Subscription {} (masterSubscriptionId={}) from event {}",
                result.subscription().getId(), event.subscriptionId(), eventId);

    }

    /**
     * subscription-service's BillingCycle is WEEKLY/MONTHLY/QUARTERLY/ANNUAL;
     * billing-service's is WEEKLY/MONTHLY/QUARTERLY/YEARLY - confirmed from
     * both services' actual source, not assumed. ANNUAL maps to YEARLY;
     * everything else matches by name.
     */
    private Subscription.BillingCycle parseBillingCycle(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("SubscriptionCreatedEvent.billingCycle was null");
        }
        return switch (raw.toUpperCase(Locale.ROOT)) {
            case "DAILY" -> Subscription.BillingCycle.DAILY;
            case "WEEKLY" -> Subscription.BillingCycle.WEEKLY;
            case "MONTHLY" -> Subscription.BillingCycle.MONTHLY;
            case "QUARTERLY" -> Subscription.BillingCycle.QUARTERLY;
            case "YEARLY" -> Subscription.BillingCycle.YEARLY;
            default -> throw new IllegalArgumentException("Unrecognized billingCycle from event: " + raw);
        };
    }
}
