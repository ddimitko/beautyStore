package com.ddimitko.beautyshopproject.controllers.payments;

import com.ddimitko.beautyshopproject.Dto.requests.PaymentRequestDto;
import com.ddimitko.beautyshopproject.services.payment.StripeService;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final StripeService stripeService;

    public PaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PutMapping("/account")
    public ResponseEntity<?> createOrRetrieveAccount(@RequestParam Long shopId) {
        try {
            Map<String, Object> response = stripeService.createOrRetrieveConnectedAccount(shopId);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/account_session")
    public ResponseEntity<?> createAccountSession(@RequestBody Map<String, Object> request) {
        String connectedAccountId = (String) request.get("account");
        System.out.println("Received request for account session: " + connectedAccountId);

        if (connectedAccountId == null || connectedAccountId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Account ID is required"));
        }

        try {
            String clientSecret = stripeService.createAccountSession(connectedAccountId);
            return ResponseEntity.ok(Map.of("client_secret", clientSecret));  // ✅ Correct response format
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Account session creation failed: " + e.getMessage()));
        }
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody PaymentRequestDto dto) {
        try {
            Map<String, String> checkoutData = stripeService.createCheckoutSession(dto);
            return ResponseEntity.ok(checkoutData);
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
