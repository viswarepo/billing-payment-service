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
@RequestMapping("/api/v1/billing/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * Called directly by Razorpay's servers, not through any authenticated
     * session - there is no X-Organization-Id here, and there can't be.
     * Must be exempted from the gateway's JwtAuthenticationGlobalFilter (see
     * api-gateway patch notes); webhook signature verification is the real
     * security control for this endpoint, not gateway JWT auth. Tenant is
     * resolved downstream, inside WebhookService, from the Payment row found
     * by Razorpay's own order/payment id - not from anything in this request.
     */
    @PostMapping("/razorpay")
    public ResponseEntity<Void> handleRazorpayWebhook(
            @RequestBody String rawPayload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        webhookService.handleWebhook(rawPayload, signature);
        return ResponseEntity.ok().build();
    }
}
