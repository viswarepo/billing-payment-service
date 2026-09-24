package com.sms.billing.dto;

import com.sms.billing.entity.Invoice;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponse {

    private String id;
    private String subscriptionId;
    private String customerId;
    private String customerName;
    private String invoiceId;
    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private Invoice.Status status;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate dueDate;
    private LocalDateTime paidAt;

    public static InvoiceResponse from(Invoice i) {
        return InvoiceResponse.builder()
                .id(i.getId())
                .subscriptionId(i.getSubscription().getId())
                .customerId(i.getSubscription().getCustomer().getId())
                .customerName(i.getSubscription().getCustomer().getName())
                .amount(i.getAmount())
                .currency(i.getCurrency())
                .status(i.getStatus())
                .periodStart(i.getPeriodStart())
                .periodEnd(i.getPeriodEnd())
                .dueDate(i.getDueDate())
                .paidAt(i.getPaidAt())
                .build();
    }
}
