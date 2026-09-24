package com.sms.billing.controller;

import com.sms.billing.dto.InvoiceResponse;
import com.sms.billing.entity.Invoice;
import com.sms.billing.repository.InvoiceRepository;
import com.sms.billing.service.InvoiceService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billing/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    private final InvoiceRepository invoiceRepository;

    @GetMapping("/{id}")
    public InvoiceResponse get(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String id) {
        return InvoiceResponse.from(invoiceService.getInvoice(organizationId, id));
    }

    @GetMapping("/sub/{subscriptionId}")
    public List<InvoiceResponse> listBySubscription(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String subscriptionId) {
        return invoiceService.listBySubscription(organizationId, subscriptionId).stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @GetMapping("/customer/{email}")
    public List<InvoiceResponse> listByCustomer(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String email) {
        return invoiceService.listByCustomer(organizationId, email).stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @GetMapping("/history")
    public List<InvoiceResponse> fullInvoiceHistory(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId)
            {
        return invoiceService.fullInvoiceHistory(organizationId).stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @GetMapping("/{invoiceId}/download")
    public ResponseEntity<InputStreamResource> downloadInvoice(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String invoiceId) {
        // Example: fetch invoice details from DB
        Invoice invoice = invoiceRepository.findByIdAndOrganizationId(invoiceId,organizationId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        return invoiceService.generateAndDownloadInvoice(
                invoice.getId(),
                invoice.getSubscription().getCustomer().getName(),
                invoice.getSubscription().getMasterSubscriptionId(),
                invoice.getAmount().doubleValue(),
                invoice.getCurrency()
        );
    }
}
