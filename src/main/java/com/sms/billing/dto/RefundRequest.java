package com.sms.billing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundRequest {

    /** Null/omitted means a full refund of the payment's captured amount. */
    private BigDecimal amount;

    @NotNull
    private String reason;
}
