package com.sms.billing.service;

import com.sms.billing.dto.CreatePlanRequest;
import com.sms.billing.entity.Plan;
import com.sms.billing.exception.ResourceNotFoundException;
import com.sms.billing.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional
    public Plan createPlan(CreatePlanRequest request,String organizationId) {
        Plan plan = Plan.builder()
                .name(request.getName())
                .amount(request.getAmount())
                .currency(request.getCurrency() == null ? "INR" : request.getCurrency())
                .billingCycle(request.getBillingCycle().name())
                .organizationId(organizationId)
                .active(true)
                .build();
        return planRepository.save(plan);
    }

    public List<Plan> listActivePlans(String organizationId) {
        return planRepository.findByActiveTrueAndOraganizationId(organizationId);
    }

    public List<Plan> getAllPlan(String organizationId) {
        return planRepository.findByOrganizationId(organizationId);
    }
    public Plan getPlan(String id, String organizationId) {
        return planRepository.findByIdAndOrganizationId(id,organizationId);
    }

    @Transactional
    public void deactivatePlan(String id, String organizationId) {
        Plan plan = getPlan(id, organizationId);
        plan.setActive(false);
        planRepository.save(plan);
    }
}
