package com.sms.billing.service;

import com.sms.billing.entity.Invoice;
import com.sms.billing.entity.Subscription;
import com.sms.billing.exception.ResourceNotFoundException;
import com.sms.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice generateForSubscription(Subscription subscription) {
        Invoice invoice = Invoice.builder()
                .subscription(subscription)
                .amount(subscription.getPlan().getAmount())
                .currency(subscription.getPlan().getCurrency())
                .status(Invoice.Status.PENDING)
                .periodStart(subscription.getCurrentPeriodStart())
                .periodEnd(subscription.getCurrentPeriodEnd())
                .dueDate(subscription.getCurrentPeriodEnd())
                .build();
        return invoiceRepository.save(invoice);
    }

    public Invoice getInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }

    public List<Invoice> listBySubscription(Long subscriptionId) {
        return invoiceRepository.findBySubscriptionId(subscriptionId);
    }

    @Transactional
    public void markPaid(Invoice invoice) {
        invoice.setStatus(Invoice.Status.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
    }

    @Transactional
    public void markFailed(Invoice invoice) {
        invoice.setStatus(Invoice.Status.FAILED);
        invoiceRepository.save(invoice);
    }
}
