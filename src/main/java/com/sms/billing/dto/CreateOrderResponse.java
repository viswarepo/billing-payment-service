package com.sms.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Everything the frontend needs to open Razorpay Checkout for an invoice. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {
    private Long paymentId;
    private String razorpayOrderId;
    private String razorpayKeyId;
    private BigDecimal amount;
    private String currency;
}
