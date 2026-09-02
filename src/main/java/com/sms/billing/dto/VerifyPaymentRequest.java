package com.sms.billing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Fields Razorpay Checkout returns to the client on success, which the client forwards to us. */
@Data
public class VerifyPaymentRequest {

    @NotBlank
    private String razorpayOrderId;

    @NotBlank
    private String razorpayPaymentId;

    @NotBlank
    private String razorpaySignature;
}
