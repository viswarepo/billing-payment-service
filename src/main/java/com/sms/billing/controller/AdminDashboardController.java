package com.sms.billing.controller;

import com.sms.billing.dto.DashboardCardResponse;
import com.sms.billing.dto.MonthlyRevenueDto;
import com.sms.billing.service.AdminDashboardService;
import com.sms.billing.service.InvoiceService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/billing/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/card")
    public Map<String, Object> getAdminDashboardCard(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId) {
        Map<String, Object> result = new HashMap<>();

        DashboardCardResponse response =adminDashboardService.getAdminDashboardCardSummary(organizationId);
        result.put("totalCustomers", response.getCountCustomer());
        result.put("totalPlan", response.getCountPlan());
        result.put("totalSubscribers", response.getCountCustomer());
        result.put("totalRevenue", response.getTotalRevenue());

        return result;
    }

    @GetMapping("/monthly/revenue")
    public List<MonthlyRevenueDto> getMonthlyRevenue(

            @RequestHeader("X-Organization-Id") @NotBlank String organizationId) {
        return adminDashboardService.getFinancialYearRevenue(organizationId);
    }
}
