package com.sms.billing.repository;

import com.sms.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findBySubscriptionId(Long subscriptionId);
    List<Invoice> findByStatus(Invoice.Status status);
}
