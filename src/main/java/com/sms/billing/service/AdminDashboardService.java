package com.sms.billing.service;

import com.sms.billing.dto.DashboardCardResponse;
import com.sms.billing.dto.MonthlyRevenueDto;
import com.sms.billing.entity.Customer;
import com.sms.billing.entity.Plan;
import com.sms.billing.entity.Subscription;
import com.sms.billing.repository.CustomerRepository;
import com.sms.billing.repository.InvoiceRepository;
import com.sms.billing.repository.PlanRepository;
import com.sms.billing.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final CustomerRepository customerRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;

    public DashboardCardResponse getAdminDashboardCardSummary(String organizationId){
        DashboardCardResponse dashboardCardResponse = new DashboardCardResponse();

        LocalDate today = LocalDate.now();
        LocalDate fyStart;
        if (today.getMonthValue() >= 4) {
            // Current year April 1
            fyStart = LocalDate.of(today.getYear(), 4, 1);
        } else {
            // Previous year April 1
            fyStart = LocalDate.of(today.getYear() - 1, 4, 1);
        }

        LocalDateTime startDate = fyStart.atStartOfDay();
        LocalDateTime endDate   = LocalDateTime.now();

        Long totalCustomers = customerRepository.countCustomers(organizationId);
        Long planCount = planRepository.findByOrganizationId(organizationId).stream().count();
        Long subscriptionCount = subscriptionRepository.findByOrganizationIdAndActiveStatus(organizationId).stream().count();

        BigDecimal totalRevenue = invoiceRepository.calculateRevenueByOrg(organizationId,startDate,endDate);

        dashboardCardResponse.setCountCustomer(totalCustomers);
        dashboardCardResponse.setCountPlan(planCount);
        dashboardCardResponse.setCountActiveSubscriber(subscriptionCount);
        dashboardCardResponse.setTotalRevenue(totalRevenue);

        return dashboardCardResponse;
    }

    public List<MonthlyRevenueDto> getFinancialYearRevenue(String orgId) {
        LocalDate today = LocalDate.now();
        LocalDate fyStart = (today.getMonthValue() >= 4)
                ? LocalDate.of(today.getYear(), 4, 1)
                : LocalDate.of(today.getYear() - 1, 4, 1);

        LocalDateTime startDate = fyStart.atStartOfDay();
        LocalDateTime endDate = LocalDateTime.now();

        List<Object[]> results = invoiceRepository.getMonthlyRevenue(orgId, startDate, endDate);

        return results.stream()
                .map(r -> new MonthlyRevenueDto((Integer) r[0], (BigDecimal) r[1]))
                .toList();
    }


}
