package com.sms.billing.repository;

import com.sms.billing.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, String> {
    List<Refund> findByOrganizationIdAndPaymentId(String organizationId, String paymentId);
}
