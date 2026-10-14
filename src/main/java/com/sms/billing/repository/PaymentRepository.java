package com.sms.billing.repository;

import com.sms.billing.entity.Invoice;
import com.sms.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByIdAndOrganizationId(String id, String organizationId);

    List<Payment> findByOrganizationIdAndInvoiceId(String organizationId, String invoiceId);

    Optional<Payment> findByRazorpayOrderIdAndOrganizationId(String razorpayOrderId, String organizatrionId);

    /**
     * Intentionally NOT organization-scoped: these back the Razorpay
     * order-creation/verify/webhook flows, which resolve the correct Payment
     * (and, from it, the correct organizationId) purely from Razorpay's own
     * identifiers - there is no request-scoped tenant context available to a
     * webhook call. PaymentService.verifyPayment adds a defense-in-depth
     * check on top of this (confirms the found row's organizationId matches
     * the caller's) even though razorpayOrderId itself is effectively
     * unguessable.
     */
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    Optional<List<Payment>> findByOrganizationId(String organizationId);

    @Query("SELECT p FROM Payment p " +
            "WHERE p.invoice.subscription.customer.customerType = :customerType")
    Optional<List<Payment>> findAllPaymentHistory(@Param("customerType") String customerType);

    @Query("SELECT p FROM Payment p " +
            "WHERE p.organizationId = :organizationId and p.invoice.subscription.customer.customerType = :customerType")
    Optional<List<Payment>> findPaymentHistoryByOrganization(@Param("organizationId") String organizationId,@Param("customerType") String customerType);
}