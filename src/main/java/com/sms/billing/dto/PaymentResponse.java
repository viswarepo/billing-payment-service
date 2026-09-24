package com.sms.billing.dto;

import com.sms.billing.entity.Invoice;
import com.sms.billing.entity.Payment;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponse {
    private String paymentId;
    private String organizationId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String paymentMethod;
    private String failureReason;

    private String invoiceId;
    private String subscriptionId;

    public static PaymentResponse from(Payment i) {
        return PaymentResponse.builder()
                .paymentId(i.getId())
                .organizationId(i.getOrganizationId())
                .invoiceId(i.getInvoice().getId())
                .subscriptionId(i.getInvoice().getSubscription().getMasterSubscriptionId())
                .amount(i.getAmount())
                .status(i.getStatus().name())
                .currency(i.getCurrency())
                .paymentMethod(i.getPaymentMethod())
                .razorpayOrderId(i.getRazorpayOrderId())
                .razorpayPaymentId(i.getRazorpayPaymentId())
                .razorpaySignature(i.getRazorpaySignature())
                .failureReason(i.getFailureReason())
                .build();
    }
}
