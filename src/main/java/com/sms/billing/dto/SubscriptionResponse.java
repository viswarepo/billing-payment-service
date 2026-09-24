package com.sms.billing.dto;

import com.sms.billing.entity.Subscription;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SubscriptionResponse {

    private String id;
    private String customerId;
    private String planId;
    private String planName;
    private Subscription.Status status;
    private LocalDate currentPeriodStart;
    private LocalDate currentPeriodEnd;
    private LocalDate nextBillingDate;

    public static SubscriptionResponse from(Subscription s) {
        return SubscriptionResponse.builder()
                .id(s.getId())
                .customerId(s.getCustomer().getId())
                .planId(s.getPlanId())
                .planName(s.getPlanId())
                .status(s.getStatus())
                .currentPeriodStart(s.getCurrentPeriodStart())
                .currentPeriodEnd(s.getCurrentPeriodEnd())
                .nextBillingDate(s.getNextBillingDate())
                .build();
    }
}
