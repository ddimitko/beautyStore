package com.ddimitko.beautyshopproject.controllers.payments;

import com.ddimitko.beautyshopproject.services.payment.StripeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final StripeService stripeService;

    public PaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @GetMapping("/account")
    public ResponseEntity<?> getConnectedAccount(@RequestParam Long shopId) {
        return ResponseEntity.ok(stripeService.getConnectedAccount(shopId));
    }

    @PutMapping("/account")
    public ResponseEntity<?> createConnectedAccount(@RequestParam Long shopId) {
        return ResponseEntity.ok(stripeService.createConnectedAccount(shopId));
    }

    @PostMapping("/account_session")
    public ResponseEntity<?> createAccountSession(@RequestBody Map<String, Object> request) {
        String connectedAccountId = (String) request.get("account");
        System.out.println("Received request for account session: " + connectedAccountId);

        if (connectedAccountId == null || connectedAccountId.isEmpty()) {
            return ResponseEntity.badRequest().body(new RuntimeException("Account ID is required"));
        }

        try {
            String clientSecret = stripeService.createAccountSession(connectedAccountId);
            return ResponseEntity.ok(new StripeService.CreateAccountSessionResponse(clientSecret));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new RuntimeException("Account session creation failed: " + e.getMessage()));
        }
    }

}
