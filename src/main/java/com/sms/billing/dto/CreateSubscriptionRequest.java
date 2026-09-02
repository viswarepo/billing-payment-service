package com.sms.billing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSubscriptionRequest {

    @NotBlank
    private String organizationId;

    @NotBlank
    private String customerName;

    @NotBlank
    @Email
    private String customerEmail;

    private String customerPhone;

    @NotNull
    private Long planId;
}
