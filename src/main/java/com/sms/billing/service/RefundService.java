package com.sms.billing.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.sms.billing.dto.RefundRequest;
import com.sms.billing.entity.Payment;
import com.sms.billing.entity.Refund;
import com.sms.billing.exception.InvalidRequestException;
import com.sms.billing.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    private final RazorpayClient razorpayClient;
    private final RefundRepository refundRepository;
    private final PaymentService paymentService;

    @Transactional
    public Refund initiateRefund(String organizationId, String paymentId, RefundRequest request) {
        Payment payment = paymentService.getPayment(organizationId, paymentId);

        if (payment.getStatus() != Payment.Status.CAPTURED
                && payment.getStatus() != Payment.Status.PARTIALLY_REFUNDED) {
            throw new InvalidRequestException(
                    "Only captured payments can be refunded (current status: " + payment.getStatus() + ")");
        }

        BigDecimal refundAmount = request.getAmount() != null ? request.getAmount() : payment.getAmount();
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new InvalidRequestException("Refund amount exceeds the original payment amount");
        }

        try {
            JSONObject refundRequestJson = new JSONObject();
            refundRequestJson.put("amount", toSmallestUnit(refundAmount));
            if (request.getReason() != null) {
                JSONObject notes = new JSONObject();
                notes.put("reason", request.getReason());
                refundRequestJson.put("notes", notes);
            }

            com.razorpay.Refund razorpayRefund =
                    razorpayClient.payments.refund(payment.getRazorpayPaymentId(), refundRequestJson);
            String refundId = razorpayRefund.get("id");

            Refund refund = Refund.builder()
                    .organizationId(organizationId)
                    .payment(payment)
                    .amount(refundAmount)
                    .status(Refund.Status.PROCESSED)
                    .razorpayRefundId(refundId)
                    .reason(request.getReason())
                    .build();
            refund = refundRepository.save(refund);

            boolean isFullRefund = refundAmount.compareTo(payment.getAmount()) == 0;
            payment.setStatus(isFullRefund ? Payment.Status.REFUNDED : Payment.Status.PARTIALLY_REFUNDED);

            return refund;

        } catch (RazorpayException e) {
            log.error("Refund failed for payment {}", paymentId, e);
            Refund failedRefund = Refund.builder()
                    .organizationId(organizationId)
                    .payment(payment)
                    .amount(refundAmount)
                    .status(Refund.Status.FAILED)
                    .reason(request.getReason())
                    .build();
            refundRepository.save(failedRefund);
            throw new InvalidRequestException("Refund could not be processed: " + e.getMessage());
        }
    }

    private long toSmallestUnit(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
