package com.sms.billing.repository;

import com.sms.billing.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// Fixed: was JpaRepository<Customer, Long>, but Customer.id is actually a
// String (GenerationType.UUID). That mismatch would fail Spring Data JPA's
// repository metamodel validation at context startup - unrelated to Kafka,
// but it blocks the app from starting at all, so it had to be fixed for
// anything (not just this consumer) to work.
public interface CustomerRepository extends JpaRepository<Customer, String> {
    /**
     * Replaces the old unscoped findByEmail(String), which was a real
     * cross-tenant bug: two orgs with a customer sharing an email would
     * collide, silently attaching a new subscription to the wrong org's
     * existing customer.
     */
    Optional<Customer> findByOrganizationIdAndEmail(String organizationId, String email);

    @Query("SELECT COUNT(c) FROM Customer c where c.organizationId =:organizationId")
    Long countCustomers(@Param("organizationId") String organizationId);
}
