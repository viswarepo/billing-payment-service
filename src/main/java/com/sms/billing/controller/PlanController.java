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
@RequestMapping("/api/v1/billing/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping("/create")
    public ResponseEntity<Plan> createPlan(
            @RequestHeader("X-Organization-Id") String organizationId,
            @Valid @RequestBody CreatePlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.createPlan(request,organizationId));
    }

    @GetMapping("/all")
    public List<Plan> listPlans(
            @RequestHeader("X-Organization-Id") String organizationId){
        return planService.getAllPlan(organizationId);
    }

    @GetMapping("/{id}")
    public Plan getPlan(@PathVariable String id,
                        @RequestHeader("X-Organization-Id") String organizationId){
        return planService.getPlan(id, organizationId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivatePlan(
            @RequestHeader("X-Organization-Id") String organizationId,
            @PathVariable String id) {
        planService.deactivatePlan(organizationId,id);
        return ResponseEntity.noContent().build();
    }
}
