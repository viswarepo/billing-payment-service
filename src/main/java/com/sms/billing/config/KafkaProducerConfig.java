package com.sms.billing.config;

import com.sms.billing.event.InvoiceGeneratedEvent;
import com.sms.billing.event.InvoicePaidEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Explicitly declares a KafkaTemplate typed for SubscriptionCreatedEvent
 * specifically, rather than relying on Spring Boot's auto-configured
 * KafkaTemplate<Object, Object> bean to satisfy a more specific generic type
 * request - that mismatch is what produced the "no qualifying bean" error.
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, InvoiceGeneratedEvent> invoiceGeneratedEventProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, InvoiceGeneratedEvent> invoiceGeneratedTemplate(
            ProducerFactory<String, InvoiceGeneratedEvent> invoiceGeneratedEventProducerFactory) {
        return new KafkaTemplate<>(invoiceGeneratedEventProducerFactory);
    }

    // ✅ InvoicePaidEvent Producer
    @Bean
    public ProducerFactory<String, InvoicePaidEvent> invoicePaidEventProducerFactoryFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, InvoicePaidEvent> invoicePaidTemplate(
            ProducerFactory<String, InvoicePaidEvent> invoicePaidEventProducerFactoryFactory) {
        return new KafkaTemplate<>(invoicePaidEventProducerFactoryFactory);
    }


}
