package com.sms.billing.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.sms.billing.entity.Invoice;
import com.sms.billing.entity.Subscription;
import com.sms.billing.exception.ResourceNotFoundException;
import com.sms.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice generateForSubscription(Subscription subscription) {
        Invoice invoice = Invoice.builder()
                .organizationId(subscription.getOrganizationId())
                .subscription(subscription)
                .amount(subscription.getAmount())
                .currency(subscription.getCurrency())
                .status(Invoice.Status.PENDING)
                .periodStart(subscription.getCurrentPeriodStart())
                .periodEnd(subscription.getCurrentPeriodEnd())
                .dueDate(subscription.getCurrentPeriodEnd())
                .build();
        return invoiceRepository.save(invoice);
    }

    public Invoice getInvoice(String organizationId, String id) {
        return invoiceRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }

    public List<Invoice> listBySubscription(String organizationId, String subscriptionId) {
        return invoiceRepository.findByOrganizationIdAndSubscriptionId(organizationId, subscriptionId);
    }

    public List<Invoice> listByCustomer(String organizationId, String email) {
        return invoiceRepository.findByOrganizationIdAndSubscription_Customer_Email(organizationId, email);
    }

    public List<Invoice> fullInvoiceHistory(String organizationId) {
        return invoiceRepository.findByOrganizationId(organizationId);
    }

    public ResponseEntity<InputStreamResource> generateAndDownloadInvoice(String invoiceId, String customerName, String subscriptionId, double amount, String currency) {
        try {
            // Generate PDF in memory
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Invoice #" + invoiceId));
            document.add(new Paragraph("Customer: " + customerName));
            document.add(new Paragraph("Subscription ID: " + subscriptionId));
            document.add(new Paragraph("Amount: " + amount + " " + currency));
            document.add(new Paragraph("Status: PAID"));
            document.add(new Paragraph("Generated on: " + java.time.LocalDate.now()));

            document.close();

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + invoiceId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(in));

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
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
