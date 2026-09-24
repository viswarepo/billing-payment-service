package com.sms.billing.controller;

import com.sms.billing.service.AdminDashboardService;
import com.sms.billing.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/billing/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public Map<String, Object> getAdminDashboard(String organizationId) {
        Map<String, Object> result = new HashMap<>();

        return result;
    }
}
