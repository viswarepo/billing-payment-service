package com.sms.billing.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.sms.billing.dto.CreateOrderResponse;
import com.sms.billing.dto.PaymentFailureRequest;
import com.sms.billing.dto.VerifyPaymentRequest;
import com.sms.billing.entity.Invoice;
import com.sms.billing.entity.Payment;
import com.sms.billing.entity.Subscription;
import com.sms.billing.exception.InvalidRequestException;
import com.sms.billing.exception.PaymentVerificationException;
import com.sms.billing.exception.ResourceNotFoundException;
import com.sms.billing.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final SubscriptionService subscriptionService;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    @Transactional
    public CreateOrderResponse createOrderForInvoice(String organizationId, String invoiceId) {
        Invoice invoice = invoiceService.getInvoice(organizationId, invoiceId);
        if (invoice.getStatus() == Invoice.Status.PAID) {
            throw new InvalidRequestException("Invoice is already paid: " + invoiceId);
        }

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", invoice.getAmount().multiply(new java.math.BigDecimal(100)));
            orderRequest.put("currency", invoice.getCurrency());
            orderRequest.put("receipt", "invoice_" + invoice.getId());

            Order order = razorpayClient.orders.create(orderRequest);
            String orderId = order.get("id");
String rp =order.get("razorpayPaymantId");
            String rp1 =order.get("razorpaySignature");

            Payment payment = Payment.builder()
                    .organizationId(organizationId)
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
     * Looks up the payment by Razorpay's own order id, confirms it belongs
     * to the calling organization (defense in depth), verifies the payment
     * signature, marks the invoice paid, and - if this was the first
     * payment for a subscription still awaiting activation - activates it.
     *
     * NOTE: this is the synchronous, client-driven confirmation path. If a
     * customer closes their browser right after paying but before this call
     * completes, the subscription would stay PENDING_ACTIVATION until your
     * Razorpay webhook handler processes the same payment asynchronously.
     * WebhookService's payment-captured handler needs the identical
     * activation check added to it for that path to work - see the manifest
     * note; I don't have current WebhookService.java to safely make that
     * change without guessing at its structure.
     */
    @Transactional
    public Payment verifyPayment(String organizationId, VerifyPaymentRequest request) throws RazorpayException {
        // Fetch payment record by Razorpay orderId
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .filter(p -> p.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No payment found for order: " + request.getRazorpayOrderId()));

     // Log incoming values for debugging
        log.info("Incoming verify request: orderId={}, paymentId={}, signature={}",
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature());

        try {
            // ✅ Validate inputs before verification
            if (request.getRazorpayOrderId() == null ||
                    request.getRazorpayPaymentId() == null ||
                    request.getRazorpaySignature() == null) {
                throw new PaymentVerificationException("Missing required fields in verify request");
            }

            // ✅ Build options for signature verification
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            // ✅ Use Razorpay Utils to verify signature
            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (!isValid) {
                payment.setStatus(Payment.Status.FAILED);
                payment.setFailureReason("Signature verification failed");
                paymentRepository.save(payment);
                throw new PaymentVerificationException(
                        "Payment signature verification failed for order " + request.getRazorpayOrderId());
            }

            // ✅ Only update payment after successful verification
            payment.setStatus(Payment.Status.CAPTURED);
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment = paymentRepository.save(payment);

            // ✅ Mark invoice as paid
            Invoice invoice = payment.getInvoice();
            invoiceService.markPaid(invoice);

            // ✅ Activate subscription if pending
            Subscription subscription = invoice.getSubscription();
            if (subscription.getStatus() == Subscription.Status.PENDING_ACTIVATION) {
                subscriptionService.activate(subscription);
            }

            return payment;

        } catch (Exception e) {
            // Catch all exceptions, not just RazorpayException
            log.error("Error verifying payment signature for order {}", request.getRazorpayOrderId(), e);
            throw new PaymentVerificationException("Could not verify payment signature", e);
        }
    }



    public Payment getPayment(String organizationId, String id) {
        return paymentRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    @Transactional
    public void recordFailure(String organizationId, PaymentFailureRequest request) {
        paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .filter(p -> p.getOrganizationId().equals(organizationId))
                .ifPresentOrElse(payment -> {
                    payment.setStatus(Payment.Status.FAILED);
                    if (request.getRazorpayPaymentId() != null) {
                        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
                    }
                    payment.setFailureReason(buildFailureReason(request));
                    paymentRepository.save(payment);
                    log.warn("Payment failed for order {}: {}",
                            request.getRazorpayOrderId(), payment.getFailureReason());
                }, () -> log.warn("Received payment.failed for unknown/foreign order {}: {} - {}",
                        request.getRazorpayOrderId(), request.getCode(), request.getDescription()));
    }

    private String buildFailureReason(PaymentFailureRequest request) {
        return String.format("[%s/%s] %s (reason: %s)",
                request.getSource(), request.getStep(), request.getDescription(), request.getReason());
    }


    public List<Payment> getPaymentHistory(String organizationId) {
        return paymentRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Details found: " + organizationId));
    }

    private long toSmallestUnit(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private String hmacSHA256(String data, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes()));
    }
}
