package com.sms.billing.dto;

import com.sms.billing.entity.Subscription;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SubscriptionResponse {

    private Long id;
    private Long customerId;
    private Long planId;
    private String planName;
    private Subscription.Status status;
    private LocalDate currentPeriodStart;
    private LocalDate currentPeriodEnd;
    private LocalDate nextBillingDate;

    public static SubscriptionResponse from(Subscription s) {
        return SubscriptionResponse.builder()
                .id(s.getId())
                .customerId(s.getCustomer().getId())
                .planId(s.getPlan().getId())
                .planName(s.getPlan().getName())
                .status(s.getStatus())
                .currentPeriodStart(s.getCurrentPeriodStart())
                .currentPeriodEnd(s.getCurrentPeriodEnd())
                .nextBillingDate(s.getNextBillingDate())
                .build();
    }
}
