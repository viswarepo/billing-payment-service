package com.sms.billing.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.sms.billing.dto.CreateOrderResponse;
import com.sms.billing.dto.VerifyPaymentRequest;
import com.sms.billing.entity.Invoice;
import com.sms.billing.entity.Payment;
import com.sms.billing.exception.InvalidRequestException;
import com.sms.billing.exception.PaymentVerificationException;
import com.sms.billing.exception.ResourceNotFoundException;
import com.sms.billing.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    /**
     * Creates a Razorpay order for the given invoice and persists a local
     * Payment record in CREATED state. The response is everything the
     * frontend needs to open Razorpay Checkout.
     */
    @Transactional
    public CreateOrderResponse createOrderForInvoice(Long invoiceId) {
        Invoice invoice = invoiceService.getInvoice(invoiceId);
        if (invoice.getStatus() == Invoice.Status.PAID) {
            throw new InvalidRequestException("Invoice is already paid: " + invoiceId);
        }

        try {
            JSONObject orderRequest = new JSONObject();
            // Razorpay expects amounts in the smallest currency unit (paise for INR).
            orderRequest.put("amount", toSmallestUnit(invoice.getAmount()));
            orderRequest.put("currency", invoice.getCurrency());
            orderRequest.put("receipt", "invoice_" + invoice.getId());

            Order order = razorpayClient.orders.create(orderRequest);
            String orderId = order.get("id");

            Payment payment = Payment.builder()
                    .invoice(invoice)
                    .amount(invoice.getAmount())
                    .currency(invoice.getCurrency())
                    .status(Payment.Status.CREATED)
                    .razorpayOrderId(orderId)
                    .build();
            payment = paymentRepository.save(payment);

            return CreateOrderResponse.builder()
                    .paymentId(payment.getId())
                    .razorpayOrderId(orderId)
                    .razorpayKeyId(razorpayKeyId)
                    .amount(invoice.getAmount())
                    .currency(invoice.getCurrency())
                    .build();

        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order for invoice {}", invoiceId, e);
            throw new InvalidRequestException("Could not create payment order: " + e.getMessage());
        }
    }

    /**
     * Verifies the signature Razorpay Checkout returns to the client after a
     * successful payment. This is the standard client-side checkout flow;
     * the webhook handler is the source of truth for server-to-server
     * confirmation and should be trusted over this for reconciliation.
     */
    @Transactional
    public Payment verifyPayment(VerifyPaymentRequest request) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No payment found for order: " + request.getRazorpayOrderId()));

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);
            if (!isValid) {
                payment.setStatus(Payment.Status.FAILED);
                payment.setFailureReason("Signature verification failed");
                paymentRepository.save(payment);
                throw new PaymentVerificationException(
                        "Payment signature verification failed for order " + request.getRazorpayOrderId());
            }

            payment.setStatus(Payment.Status.CAPTURED);
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment = paymentRepository.save(payment);

            invoiceService.markPaid(payment.getInvoice());

            return payment;

        } catch (RazorpayException e) {
            log.error("Error verifying payment signature for order {}", request.getRazorpayOrderId(), e);
            throw new PaymentVerificationException("Could not verify payment signature", e);
        }
    }

    public Payment getPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    /** Razorpay amounts are always in the smallest currency unit (e.g. paise for INR, cents for USD). */
    private long toSmallestUnit(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
