import { useState, useEffect } from "react";
import { loadConnectAndInitialize } from "@stripe/connect-js";

export const useStripeConnect = (connectedAccountId) => {
    const [stripeConnectInstance, setStripeConnectInstance] = useState(null);

    useEffect(() => {
        if (!connectedAccountId) return;

        const fetchClientSecret = async () => {
            try {
                const response = await fetch("http://localhost:8080/api/payments/account_session", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    credentials: "include",
                    body: JSON.stringify({ account: connectedAccountId }), // ✅ Ensure correct request format
                });

                if (!response.ok) {
                    const errorResponse = await response.json();
                    console.error("Error fetching client secret:", errorResponse.error);
                    return null;
                }

                const jsonResponse = await response.json();
                return jsonResponse.client_secret;  // ✅ Use directly, no need for extra JSON parsing

            } catch (error) {
                console.error("Failed to fetch client secret:", error);
                return null;
            }
        };

        const initializeStripeConnect = async () => {
            const clientSecret = await fetchClientSecret();
            if (!clientSecret) return;

            setStripeConnectInstance(
                loadConnectAndInitialize({
                    publishableKey: process.env.REACT_APP_STRIPE_PUBLISHABLE_KEY,
                    fetchClientSecret: () => clientSecret,  // ✅ Pass plain string clientSecret
                    appearance: {
                        overlays: "dialog",
                        variables: {
                            colorPrimary: "#635BFF",
                        },
                    },
                })
            );
        };

        initializeStripeConnect();
    }, [connectedAccountId]);

    return stripeConnectInstance;
};

