package com.sms.billing.repository;

import com.sms.billing.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByCustomerId(Long customerId);

    List<Subscription> findByStatusAndNextBillingDateLessThanEqual(
            Subscription.Status status, LocalDate date);
}
