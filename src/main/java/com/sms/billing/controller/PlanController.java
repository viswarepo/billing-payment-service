package com.sms.billing.controller;

import com.sms.billing.dto.CreatePlanRequest;
import com.sms.billing.entity.Plan;
import com.sms.billing.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    public ResponseEntity<Plan> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.createPlan(request));
    }

    @GetMapping
    public List<Plan> listPlans() {
        return planService.listActivePlans();
    }

    @GetMapping("/{id}")
    public Plan getPlan(@PathVariable Long id) {
        return planService.getPlan(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivatePlan(@PathVariable Long id) {
        planService.deactivatePlan(id);
        return ResponseEntity.noContent().build();
    }
}
