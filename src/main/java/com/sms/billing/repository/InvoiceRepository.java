package com.sms.billing.repository;

import com.sms.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    Optional<Invoice> findByIdAndOrganizationId(String id, String organizationId);

    List<Invoice> findByOrganizationIdAndSubscriptionId(String organizationId, String subscriptionId);

    List<Invoice> findByOrganizationIdAndStatus(String organizationId, Invoice.Status status);

    List<Invoice> findByOrganizationIdAndSubscription_Customer_Id(String organizationId, String customerId);

    List<Invoice> findByOrganizationIdAndSubscription_Customer_Email(String organizationId, String email);

    List<Invoice> findByOrganizationIdAndSubscription_Customer_CustomerType(String organizationId, String customerType);

    List<Invoice> findBySubscription_Customer_CustomerType(String customerType);

    List<Invoice> findByOrganizationId(String organizationId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) " +
            "FROM Invoice i " +
            "WHERE i.status = com.sms.billing.entity.Invoice.Status.PAID and i.organizationId=:organizationId")
    BigDecimal calculateTotalRevenue(@Param("organizationId") String organizationId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) " +
            "FROM Invoice i " +
            "WHERE i.status = com.sms.billing.entity.Invoice.Status.PAID " +
            "AND i.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueForPeriod(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(i.amount), 0) " +
            "FROM Invoice i " +
            "WHERE i.organizationId = :orgId " +
            "AND i.status = com.sms.billing.entity.Invoice.Status.PAID " +
            "AND i.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueByOrg(@Param("orgId") String orgId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT MONTH(i.paidAt), COALESCE(SUM(i.amount), 0) " +
            "FROM Invoice i " +
            "WHERE i.organizationId = :orgId " +
            "AND i.status = com.sms.billing.entity.Invoice.Status.PAID " +
            "AND i.paidAt BETWEEN :startDate AND :endDate " +
            "GROUP BY MONTH(i.paidAt) " +
            "ORDER BY MONTH(i.paidAt)")
    List<Object[]> getMonthlyRevenue(@Param("orgId") String orgId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);



}
