package com.sms.billing.producer;


import com.sms.billing.event.InvoicePaidEvent;
import com.sms.billing.event.SubscriptionCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertInvoicePaidEventProducer {

    private final KafkaTemplate<String, InvoicePaidEvent> kafkaTemplate;

    public AlertInvoicePaidEventProducer(KafkaTemplate<String, InvoicePaidEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void alertInvoicePaid(InvoicePaidEvent event) {
        kafkaTemplate.send("alert.invoice.paid", event);
    }
}

