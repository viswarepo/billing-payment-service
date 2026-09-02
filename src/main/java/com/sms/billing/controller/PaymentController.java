package com.sms.billing.controller;

import com.sms.billing.dto.CreateOrderResponse;
import com.sms.billing.dto.RefundRequest;
import com.sms.billing.dto.VerifyPaymentRequest;
import com.sms.billing.entity.Payment;
import com.sms.billing.entity.Refund;
import com.sms.billing.service.PaymentService;
import com.sms.billing.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService;

    @PostMapping("/orders")
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestParam Long invoiceId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createOrderForInvoice(invoiceId));
    }

    @PostMapping("/verify")
    public Payment verify(@Valid @RequestBody VerifyPaymentRequest request) {
        return paymentService.verifyPayment(request);
    }

    @GetMapping("/{id}")
    public Payment get(@PathVariable Long id) {
        return paymentService.getPayment(id);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<Refund> refund(@PathVariable Long id, @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(refundService.initiateRefund(id, request));
    }
}
