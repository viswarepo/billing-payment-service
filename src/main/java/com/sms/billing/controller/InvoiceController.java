package com.sms.billing.controller;

import com.sms.billing.dto.InvoiceResponse;
import com.sms.billing.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/{id}")
    public InvoiceResponse get(@PathVariable Long id) {
        return InvoiceResponse.from(invoiceService.getInvoice(id));
    }

    @GetMapping
    public List<InvoiceResponse> listBySubscription(@RequestParam Long subscriptionId) {
        return invoiceService.listBySubscription(subscriptionId).stream()
                .map(InvoiceResponse::from)
                .toList();
    }
}
