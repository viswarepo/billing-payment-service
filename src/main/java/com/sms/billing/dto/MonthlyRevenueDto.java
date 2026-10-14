package com.sms.billing.dto;

import java.math.BigDecimal;

public class MonthlyRevenueDto {
    private int month;          // numeric month (1–12)
    private BigDecimal revenue; // total revenue for that month

    public MonthlyRevenueDto(int month, BigDecimal revenue) {
        this.month = month;
        this.revenue = revenue;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}

