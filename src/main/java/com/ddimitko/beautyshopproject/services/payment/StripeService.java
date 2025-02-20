package com.ddimitko.beautyshopproject.services.payment;

import com.ddimitko.beautyshopproject.entities.Shop;
import com.ddimitko.beautyshopproject.services.ShopService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.stripe.Stripe;
import com.stripe.model.Account;
import com.stripe.model.AccountSession;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountSessionCreateParams;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    private final ShopService shopService;
    private final Gson gson = new Gson();

    public StripeService(ShopService shopService, @Value("#{environment.stripeApiKey}") String stripeApiKey) {
        this.shopService = shopService;
        Stripe.apiKey = stripeApiKey;
    }

    public String getConnectedAccount(Long shopId) {
        Shop shop = shopService.findShopById(shopId);

        if (shop != null && shop.getStripeAccountId() != null) {
            return gson.toJson(new CreateAccountResponse(shop.getStripeAccountId()));
        }

        return gson.toJson(new CreateAccountResponse(null));
    }

    public String createConnectedAccount(Long shopId) {
        Shop shop = shopService.findShopById(shopId);

        if (shop == null) {
            return gson.toJson(new ErrorResponse("Shop not found"));
        }

        // Check if shop already has a connected account
        if (shop.getStripeAccountId() != null) {
            return gson.toJson(new CreateAccountResponse(shop.getStripeAccountId()));
        }

        try {
            Account account = Account.create(
                    AccountCreateParams.builder()
                            .setEmail(shop.getOwner().getEmail())
                            .setBusinessProfile(AccountCreateParams.BusinessProfile.builder()
                                    .setMcc("7230")
                                    .setName(shop.getName()).build())
                            .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL)
                            .setType(AccountCreateParams.Type.CUSTOM)
                            .setCapabilities(AccountCreateParams.Capabilities.builder()
                                    .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder()
                                            .setRequested(true)
                                            .build())
                                    .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                            .setRequested(true)
                                            .build())
                                    .build())
                            .setCountry("BG")
                            .build()
            );

            // Save the connected account ID to the shop
            shop.setStripeAccountId(account.getId());
            shopService.saveShop(shop);

            return gson.toJson(new CreateAccountResponse(account.getId()));
        } catch (Exception e) {
            return gson.toJson(new ErrorResponse("Stripe account creation failed: " + e.getMessage()));
        }
    }

    public String createAccountSession(String connectedAccountId) {
        try {
            AccountSession accountSession = AccountSession.create(
                    AccountSessionCreateParams.builder()
                            .setAccount(connectedAccountId)
                            .setComponents(AccountSessionCreateParams.Components.builder()
                                    .setAccountOnboarding(AccountSessionCreateParams.Components.AccountOnboarding.builder()
                                            .setEnabled(true)
                                            .build())
                                    .build())
                            .build()
            );

            return accountSession.getClientSecret();  // Return only the client_secret
        } catch (Exception e) {
            throw new RuntimeException("Account session creation failed: " + e.getMessage());
        }
    }

    // Response classes for API responses
    private static class ErrorResponse {
        @Getter
        private String error;
        public ErrorResponse(String error) { this.error = error; }
    }

    private static class CreateAccountResponse {
        @Getter
        private String account;
        public CreateAccountResponse(String account) { this.account = account; }
    }

    public static class CreateAccountSessionResponse {
        private String clientSecret;

        @JsonCreator
        public CreateAccountSessionResponse(@JsonProperty("client_secret") String clientSecret) {
            this.clientSecret = clientSecret;
        }

        // Getter and setter
        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }

}
