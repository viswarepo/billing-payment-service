package com.sms.billing.service;

import com.sms.billing.entity.Invoice;
import com.sms.billing.entity.Subscription;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private final SubscriptionService subscriptionService;
    private final InvoiceService invoiceService;

    /**
     * For every active subscription whose next billing date has arrived:
     * generate an invoice for the period just completed, then roll the
     * subscription forward to its next period.
     *
     * This deliberately does NOT auto-charge the customer - it only raises
     * the invoice. Actually collecting payment happens via
     * PaymentService.createOrderForInvoice (customer-initiated checkout) or
     * a separate auto-charge job against a saved Razorpay mandate, which is
     * out of scope here since it depends on how recurring mandates are set
     * up on the Razorpay side.
     */
    @Transactional
    public BillingCycleResult processBillingCycle() {
        LocalDate today = LocalDate.now();
        List<Subscription> due = subscriptionService.findDueForBilling(today);

        int invoicesGenerated = 0;
        int failures = 0;

        for (Subscription subscription : due) {
            try {
                Invoice invoice = invoiceService.generateForSubscription(subscription);
                subscriptionService.advanceBillingPeriod(subscription);
                invoicesGenerated++;
                log.info("Generated invoice {} for subscription {}", invoice.getId(), subscription.getId());
            } catch (Exception e) {
                failures++;
                log.error("Failed to process billing for subscription {}", subscription.getId(), e);
                subscriptionService.markPastDue(subscription);
            }
        }

        log.info("Billing cycle complete: {} due, {} invoiced, {} failed",
                due.size(), invoicesGenerated, failures);

        return new BillingCycleResult(due.size(), invoicesGenerated, failures);
    }

    public record BillingCycleResult(int subscriptionsDue, int invoicesGenerated, int failures) {
    }
}
