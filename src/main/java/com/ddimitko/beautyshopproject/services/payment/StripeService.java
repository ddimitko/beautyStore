package com.ddimitko.beautyshopproject.services.payment;

import com.ddimitko.beautyshopproject.Dto.requests.PaymentRequestDto;
import com.ddimitko.beautyshopproject.entities.Shop;
import com.ddimitko.beautyshopproject.services.ServicesService;
import com.ddimitko.beautyshopproject.services.ShopService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountSession;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    private final ShopService shopService;
    private final ServicesService servicesService;

    public StripeService(ShopService shopService, @Value("#{environment.stripeApiKey}") String stripeApiKey, ServicesService servicesService) {
        this.shopService = shopService;
        this.servicesService = servicesService;
        Stripe.apiKey = stripeApiKey;
    }

    public Map<String, Object> createOrRetrieveConnectedAccount(Long shopId) throws StripeException {
            // Check if the shop already has a Stripe account in database
            Shop shop = shopService.getShopById(shopId);
            String connectedAccountId = shop.getStripeAccountId(); // Fetch from DB

            if (connectedAccountId == null) {
                // If no Stripe account is related to that shop, create one
                AccountCreateParams params = AccountCreateParams.builder()
                        .setEmail(shop.getOwner().getEmail())
                        .setBusinessProfile(AccountCreateParams.BusinessProfile.builder()
                                .setMcc("7230") // 7230 Stands for 'Barber and Beauty Shops'
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
                                .setRevolutPayPayments(AccountCreateParams.Capabilities.RevolutPayPayments.builder()
                                        .setRequested(true)
                                        .build())
                                .build())
                        .setCountry("BG")
                        .build();

                Account account = Account.create(params);
                shop.setStripeAccountId(account.getId());
                shopService.saveShop(shop);

                connectedAccountId = account.getId();
            }

            // Retrieve the account from Stripe
            Account account = Account.retrieve(connectedAccountId);

            // Check onboarding status
            boolean chargesEnabled = account.getChargesEnabled();
            boolean detailsSubmitted = account.getDetailsSubmitted();

            // Prepare the response data
            Map<String, Object> response = new HashMap<>();
            response.put("account", connectedAccountId);
            response.put("chargesEnabled", chargesEnabled);
            response.put("detailsSubmitted", detailsSubmitted);

            return response;
    }

    public String createAccountSession(String connectedAccountId) throws StripeException {
        Account account = Account.retrieve(connectedAccountId);

        // Check if the account has already completed onboarding
        boolean isOnboarded = Boolean.TRUE.equals(account.getChargesEnabled()) && Boolean.TRUE.equals(account.getDetailsSubmitted());

        try {
            AccountSessionCreateParams.Builder sessionBuilder = AccountSessionCreateParams.builder()
                    .setAccount(connectedAccountId);

            AccountSessionCreateParams.Components.Builder componentsBuilder = AccountSessionCreateParams.Components.builder();

            if (!isOnboarded) {
                // Set onboarding if not completed
                componentsBuilder.setAccountOnboarding(
                        AccountSessionCreateParams.Components.AccountOnboarding.builder()
                                .setEnabled(true)
                                .build()
                );
            } else {
                // Set payments if onboarding is completed
                componentsBuilder.setPayments(
                        AccountSessionCreateParams.Components.Payments.builder()
                                .setEnabled(true)
                                .setFeatures(
                                        AccountSessionCreateParams.Components.Payments.Features.builder()
                                                .setRefundManagement(true)
                                                .setDisputeManagement(true)
                                                .setCapturePayments(true)
                                                .build()
                                )
                                .build()
                );
            }

            AccountSession accountSession = AccountSession.create(sessionBuilder
                    .setComponents(componentsBuilder.build())
                    .build()
            );


            return accountSession.getClientSecret();

        } catch (Exception e) {
            throw new RuntimeException("Account session creation failed: " + e.getMessage());
        }
    }


    public Map<String, String> createCheckoutSession(PaymentRequestDto payment) throws StripeException {
        Shop shop = shopService.getShopById(payment.getShopId());
        com.ddimitko.beautyshopproject.entities.Service service = servicesService.getServiceById(payment.getServiceId());

        if (service.getPrice() == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        long amountInStotinki = service.getPrice().multiply(BigDecimal.valueOf(100)).longValue();

        SessionCreateParams.Builder paramsBuilder =
                SessionCreateParams.builder()
                        .setExpiresAt(ZonedDateTime.now().plusMinutes(30).toEpochSecond())
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("bgn")
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName(service.getName())
                                                                        .build()
                                                        )
                                                        .setUnitAmount(amountInStotinki)
                                                        .build()
                                        )
                                        .setQuantity(1L)
                                        .build()
                        )
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                        .setCustomerEmail(payment.getCustomerEmail())
                        .setRedirectOnCompletion(SessionCreateParams.RedirectOnCompletion.NEVER);

        RequestOptions requestOptions =
                RequestOptions.builder().setStripeAccount(shop.getStripeAccountId()).build();

        SessionCreateParams params = paramsBuilder.build();

        try {
            Session session = Session.create(params, requestOptions);

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", session.getClientSecret());
            response.put("connectedAccountId", requestOptions.getStripeAccount());

            return response;
        } catch (StripeException e) {
            System.err.println("Error creating checkout session: " + e.getMessage());
            throw e;
        }
    }

}
