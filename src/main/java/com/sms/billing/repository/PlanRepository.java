package com.sms.billing.repository;

import com.sms.billing.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, String> {
    @Query("SELECT p FROM Plan p WHERE p.active = true AND p.organizationId = :organizationId")
    List<Plan> findByActiveTrueAndOraganizationId(@Param("organizationId") String organizationId);

    Plan findByIdAndOrganizationId(String Id, String organizationId);

    List<Plan> findByOrganizationId(String organizationId);

    boolean existsByPlanIdAndOrganizationId(String planId, String organizationId);
}
