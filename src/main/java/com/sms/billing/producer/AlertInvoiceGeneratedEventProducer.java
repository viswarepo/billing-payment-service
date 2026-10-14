package com.sms.billing.producer;


import com.sms.billing.event.InvoiceGeneratedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertInvoiceGeneratedEventProducer {

    private final KafkaTemplate<String, InvoiceGeneratedEvent> kafkaTemplate;

    public AlertInvoiceGeneratedEventProducer(KafkaTemplate<String, InvoiceGeneratedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void alertInvoiceGenerated(InvoiceGeneratedEvent event) {
        kafkaTemplate.send("alert.invoice.generated", event);
    }
}

