package com.sms.billing.repository;

import com.sms.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    Optional<Invoice> findByIdAndOrganizationId(String id, String organizationId);

    List<Invoice> findByOrganizationIdAndSubscriptionId(String organizationId, String subscriptionId);

    List<Invoice> findByOrganizationIdAndStatus(String organizationId, Invoice.Status status);

    List<Invoice> findByOrganizationIdAndSubscription_Customer_Id(String organizationId, String customerId);

    List<Invoice> findByOrganizationIdAndSubscription_Customer_Email(String organizationId, String email);

    List<Invoice> findByOrganizationId(String organizationId);

}
