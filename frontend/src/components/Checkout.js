import React, {useMemo} from "react";
import { loadStripe } from "@stripe/stripe-js";
import {
    EmbeddedCheckoutProvider,
    EmbeddedCheckout
} from "@stripe/react-stripe-js";

export default function Checkout({ connectedAccountId, clientSecret }) {

    // ✅ Only create stripePromise when connectedAccountId is available
    const stripePromise = useMemo(() => {
        if (!connectedAccountId) return null;
        return loadStripe(process.env.REACT_APP_STRIPE_PUBLISHABLE_KEY, {
            stripeAccount: connectedAccountId,
        });
    }, [connectedAccountId]);  // Runs only when connectedAccountId changes

    return (
        <EmbeddedCheckoutProvider stripe={stripePromise} options={{clientSecret}}>
            <EmbeddedCheckout />
        </EmbeddedCheckoutProvider>
    );
}
