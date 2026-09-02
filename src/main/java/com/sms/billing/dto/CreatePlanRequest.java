package com.sms.billing.dto;

import com.sms.billing.entity.Plan;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePlanRequest {

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    private String currency = "INR";

    @NotNull
    private Plan.BillingCycle billingCycle;
}
