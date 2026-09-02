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
    public Plan createPlan(CreatePlanRequest request) {
        Plan plan = Plan.builder()
                .name(request.getName())
                .amount(request.getAmount())
                .currency(request.getCurrency() == null ? "INR" : request.getCurrency())
                .billingCycle(request.getBillingCycle())
                .active(true)
                .build();
        return planRepository.save(plan);
    }

    public List<Plan> listActivePlans() {
        return planRepository.findByActiveTrue();
    }

    public Plan getPlan(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + id));
    }

    @Transactional
    public void deactivatePlan(Long id) {
        Plan plan = getPlan(id);
        plan.setActive(false);
        planRepository.save(plan);
    }
}
