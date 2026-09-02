package com.sms.billing.scheduler;

import com.sms.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BillingCycleScheduler {

    private static final Logger log = LoggerFactory.getLogger(BillingCycleScheduler.class);

    private final BillingService billingService;

    /** Runs once a day at 02:00 server time. Also triggerable manually via the admin endpoint. */
    @Scheduled(cron = "${billing.cycle-cron:0 0 2 * * *}")
    public void runScheduledBillingCycle() {
        log.info("Starting scheduled billing cycle run");
        billingService.processBillingCycle();
    }
}
