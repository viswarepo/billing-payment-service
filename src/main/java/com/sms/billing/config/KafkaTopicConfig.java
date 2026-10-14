package com.sms.billing.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String SUBSCRIPTION_CREATED_TOPIC = "subscription.created";


    @Bean
    public NewTopic alertInvoiceGeneratedTopic() {
        return TopicBuilder.name("alert.invoice.generated")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic alertInvoicePaidTopic() {
        return TopicBuilder.name("alert.invoice.paid")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic alertPaymentFailedTopic() {
        return TopicBuilder.name("alert.payment.failed")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic alertPaymentSuccessTopic() {
        return TopicBuilder.name("alert.payment.success")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic alertRefundProcessedTopic() {
        return TopicBuilder.name("alert.refund.processed")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic alertBillingCycleChangedTopic() {
        return TopicBuilder.name("alert.billingcycle.changed")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic alertPlanPriceChangedTopic() {
        return TopicBuilder.name("alert.planprice.changed")
                .partitions(3)
                .replicas(1)
                .build();
    }

}
