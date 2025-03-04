import React, {useCallback, useMemo, useState} from "react";
import { loadStripe } from "@stripe/stripe-js";
import {
    EmbeddedCheckoutProvider,
    EmbeddedCheckout
} from "@stripe/react-stripe-js";

export default function Checkout({ connectedAccountId, clientSecret, sessionToken, onNext}) {
    const [isSubmitting, setIsSubmitting] = useState(false);

    console.log(connectedAccountId);
    console.log(clientSecret);
    console.log(sessionToken);

    // ✅ Only create stripePromise when connectedAccountId is available
    const stripePromise = useMemo(() => {
        if (!connectedAccountId) return null;
        return loadStripe(process.env.REACT_APP_STRIPE_PUBLISHABLE_KEY, {
            stripeAccount: connectedAccountId,
        });
    }, [connectedAccountId]);  // Runs only when connectedAccountId changes

    const handleCardSuccess = useCallback(async () => {
        setIsSubmitting(true);

        const sessionToken = sessionStorage.getItem("sessionToken");

        if (!sessionToken) {
            setIsSubmitting(false);
            return;
        }

        console.log(sessionToken);

        try {
            const response = await fetch("http://localhost:8080/api/appointment/confirm", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${sessionToken}`  // Send session token in headers
                }
            });

            if (!response.ok) {
                const errorMessage = await response.text();
                throw new Error(errorMessage || "Booking failed");
            }

            sessionStorage.removeItem("sessionToken");

            onNext.current.nextCallback();

        } catch (error) {
            console.log(error);
        } finally {
            setIsSubmitting(false);
        }
    }, [sessionToken, onNext]);

    return (
        <EmbeddedCheckoutProvider stripe={stripePromise} options={{
            clientSecret,
            onComplete: handleCardSuccess
        }}>
            <EmbeddedCheckout/>
        </EmbeddedCheckoutProvider>
    );
}
