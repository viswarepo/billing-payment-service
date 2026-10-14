package com.sms.billing.dto;

import com.sms.billing.entity.Invoice;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DashboardCardResponse {

    private Long countCustomer;
    private Long countPlan;
    private Long countActiveSubscriber;
    private BigDecimal totalRevenue;

}
