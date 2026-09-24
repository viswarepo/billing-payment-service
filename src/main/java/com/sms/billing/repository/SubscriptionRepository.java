package com.sms.billing.repository;

import com.sms.billing.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    Optional<Subscription> findByIdAndOrganizationId(String id, String organizationId);

    List<Subscription> findByOrganizationIdAndCustomerId(String organizationId, String customerId);

    /**
     * Intentionally NOT organization-scoped: the billing run is a cross-tenant
     * operational job (see BillingService.processBillingCycle), not a
     * tenant-facing read. Each Subscription row it touches still carries its
     * own organizationId.
     */
    List<Subscription> findByStatusAndNextBillingDateLessThanEqual(
            Subscription.Status status, LocalDate date);
}
