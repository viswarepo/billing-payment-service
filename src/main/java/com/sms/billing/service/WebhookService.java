package com.sms.billing.service;

import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.sms.billing.entity.Payment;
import com.sms.billing.exception.PaymentVerificationException;
import com.sms.billing.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    /**
     * Verifies the X-Razorpay-Signature header against the raw request body,
     * then processes the event. The raw body (not a re-serialized object)
     * MUST be used for signature verification, or valid webhooks will fail.
     */
    @Transactional
    public void handleWebhook(String rawPayload, String signature) {
        boolean isValid;
        try {
            isValid = Utils.verifyWebhookSignature(rawPayload, signature, webhookSecret);
        } catch (RazorpayException e) {
            log.error("Webhook signature verification threw an exception", e);
            throw new PaymentVerificationException("Could not verify webhook signature", e);
        }

        if (!isValid) {
            throw new PaymentVerificationException("Invalid webhook signature");
        }

        JSONObject payload = new JSONObject(rawPayload);
        String event = payload.optString("event", "");
        log.info("Received verified Razorpay webhook event: {}", event);

        switch (event) {
            case "payment.captured" -> handlePaymentCaptured(payload);
            case "payment.failed" -> handlePaymentFailed(payload);
            default -> log.info("Ignoring unhandled webhook event type: {}", event);
        }
    }

    private void handlePaymentCaptured(JSONObject payload) {
        String razorpayPaymentId = extractPaymentId(payload);
        String razorpayOrderId = extractOrderId(payload);
        String organizationId = extractOrganizationId(payload);

        paymentRepository.findByRazorpayOrderIdAndOrganizationId(razorpayOrderId, organizationId)
                .ifPresentOrElse(payment -> {
                    if (payment.getStatus() == Payment.Status.CAPTURED) {
                        // Already handled via client-side verify flow
                        return;
                    }
                    payment.setStatus(Payment.Status.CAPTURED);
                    payment.setRazorpayPaymentId(razorpayPaymentId);
                    paymentRepository.save(payment);
                    invoiceService.markPaid(payment.getInvoice());
                }, () -> log.warn("Webhook payment.captured for unknown order {}", razorpayOrderId));
    }

    private void handlePaymentFailed(JSONObject payload) {
        String razorpayOrderId = extractOrderId(payload);
        String organizationid = extractOrganizationId(payload);

        paymentRepository.findByRazorpayOrderIdAndOrganizationId(razorpayOrderId,organizationid).ifPresentOrElse(payment -> {
            payment.setStatus(Payment.Status.FAILED);
            payment.setFailureReason(extractErrorDescription(payload));
            paymentRepository.save(payment);
            invoiceService.markFailed(payment.getInvoice());
        }, () -> log.warn("Webhook payment.failed for unknown order {}", razorpayOrderId));
    }

    private String extractPaymentId(JSONObject payload) {
        return payload.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity")
                .getString("id");
    }
    private String extractOrganizationId(JSONObject payload) {
        return payload.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity")
                .getString("organizationId");
    }

    private String extractOrderId(JSONObject payload) {
        return payload.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity")
                .optString("order_id", null);
    }

    private String extractErrorDescription(JSONObject payload) {
        return payload.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity")
                .optString("error_description", "Unknown failure");
    }
}
