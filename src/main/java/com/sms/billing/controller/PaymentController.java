package com.sms.billing.controller;

import com.razorpay.RazorpayException;
import com.sms.billing.dto.*;
import com.sms.billing.entity.Payment;
import com.sms.billing.entity.Refund;
import com.sms.billing.service.PaymentService;
import com.sms.billing.service.RefundService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billing/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService;

    @PostMapping("/orders")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @RequestParam String invoiceId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createOrderForInvoice(organizationId, invoiceId));
    }

    @PostMapping("/verify")
    public Payment verify(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @Valid @RequestBody VerifyPaymentRequest request) throws RazorpayException {
        return paymentService.verifyPayment(organizationId, request);
    }

    @GetMapping("/{id}")
    public Payment get(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String id) {
        return paymentService.getPayment(organizationId, id);
    }

    /** Restricted to ADMIN at the gateway - customers shouldn't be able to self-refund. */
    @PostMapping("/{id}/refund")
    public ResponseEntity<Refund> refund(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String id,
            @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(refundService.initiateRefund(organizationId, id, request));
    }

    @PostMapping("/record-failure")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordFailure(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @Valid @RequestBody PaymentFailureRequest request) {
        paymentService.recordFailure(organizationId, request);
    }


    @GetMapping("/history")
    public List<PaymentResponse> paymentHistory(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId){
         return paymentService.getPaymentHistory(organizationId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

}
