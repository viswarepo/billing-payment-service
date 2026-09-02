package com.sms.billing.controller;

import com.sms.billing.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * Razorpay webhook endpoint. Must receive the RAW request body (not a
     * parsed/re-serialized object) since signature verification is computed
     * over the exact bytes Razorpay sent.
     */
    @PostMapping("/razorpay")
    public ResponseEntity<Void> handleRazorpayWebhook(
            @RequestBody String rawPayload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        webhookService.handleWebhook(rawPayload, signature);
        return ResponseEntity.ok().build();
    }
}
