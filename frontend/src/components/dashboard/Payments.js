import React from "react";
import {
    ConnectAccountOnboarding,
    ConnectComponentsProvider,
    ConnectPayments,
} from "@stripe/react-connect-js";

import { useState, useEffect } from "react";
import { useStripeConnect } from "../hooks/useStripeConnect"
import { useLocation } from "react-router-dom";

export default function Payments() {
    const [accountCreatePending, setAccountCreatePending] = useState(false);
    const [onboardingExited, setOnboardingExited] = useState(false);
    const [error, setError] = useState(false);
    const [connectedAccountId, setConnectedAccountId] = useState(null);
    const [chargesEnabled, setChargesEnabled] = useState(false);
    const [detailsSubmitted, setDetailsSubmitted] = useState(false);
    const [loading, setLoading] = useState(true);

    const stripeConnectInstance = useStripeConnect(connectedAccountId);
    const location = useLocation();
    const { shopId } = location.state || {};

    // Fetch or create a connected account
    useEffect(() => {
        const fetchAccount = async () => {
            try {
                setLoading(true);
                const response = await fetch(`http://localhost:8080/api/payments/account?shopId=${shopId}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    credentials: "include"
                });

                const data = await response.json();
                console.log(data);

                if (data.account) {
                    setConnectedAccountId(data.account);
                    setChargesEnabled(data.chargesEnabled);
                    setDetailsSubmitted(data.detailsSubmitted);
                } else {
                    setError(true);
                }
            } catch (err) {
                console.error("Error creating/retrieving account:", err);
                setError(true);
            } finally {
                setAccountCreatePending(false);
                setLoading(false);
            }
        };

        if (!connectedAccountId) {
            setAccountCreatePending(true);
            fetchAccount();
        }
    }, [shopId, connectedAccountId]);

    return (
        <div className="flex justify-center container">
            <div className="content">
                {/* 🔥 Show Loading Spinner while fetching */}
                {loading && (
                    <div className="loading-container">
                        <div role="status">
                            <svg aria-hidden="true"
                                 className="inline w-8 h-8 text-gray-200 animate-spin dark:text-gray-600 fill-gray-600 dark:fill-gray-300"
                                 viewBox="0 0 100 101" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path
                                    d="M100 50.5908C100 78.2051 77.6142 100.591 50 100.591C22.3858 100.591 0 78.2051 0 50.5908C0 22.9766 22.3858 0.59082 50 0.59082C77.6142 0.59082 100 22.9766 100 50.5908ZM9.08144 50.5908C9.08144 73.1895 27.4013 91.5094 50 91.5094C72.5987 91.5094 90.9186 73.1895 90.9186 50.5908C90.9186 27.9921 72.5987 9.67226 50 9.67226C27.4013 9.67226 9.08144 27.9921 9.08144 50.5908Z"
                                    fill="currentColor"/>
                                <path
                                    d="M93.9676 39.0409C96.393 38.4038 97.8624 35.9116 97.0079 33.5539C95.2932 28.8227 92.871 24.3692 89.8167 20.348C85.8452 15.1192 80.8826 10.7238 75.2124 7.41289C69.5422 4.10194 63.2754 1.94025 56.7698 1.05124C51.7666 0.367541 46.6976 0.446843 41.7345 1.27873C39.2613 1.69328 37.813 4.19778 38.4501 6.62326C39.0873 9.04874 41.5694 10.4717 44.0505 10.1071C47.8511 9.54855 51.7191 9.52689 55.5402 10.0491C60.8642 10.7766 65.9928 12.5457 70.6331 15.2552C75.2735 17.9648 79.3347 21.5619 82.5849 25.841C84.9175 28.9121 86.7997 32.2913 88.1811 35.8758C89.083 38.2158 91.5421 39.6781 93.9676 39.0409Z"
                                    fill="currentFill"/>
                            </svg>
                            <span className="sr-only">Loading...</span>
                        </div>
                    </div>
                )}
                {/* Show sign-up section if no account exists */}
                {!loading && !connectedAccountId && (
                    <>
                        <h2>Get ready for takeoff</h2>
                        <p>Rocket Rides is the world's leading air travel platform: join our team of pilots to help
                            people travel faster.</p>
                        {!accountCreatePending &&
                            <button onClick={() => setAccountCreatePending(true)}>Get Started!</button>}
                    </>
                )}

                {/* Show onboarding if the account exists but is not fully onboarded */}
                {!loading && connectedAccountId && stripeConnectInstance && (
                    <ConnectComponentsProvider connectInstance={stripeConnectInstance}>
                        {!chargesEnabled && !detailsSubmitted && (
                            <ConnectAccountOnboarding onExit={() => setOnboardingExited(true)}/>
                        )}
                        {chargesEnabled && detailsSubmitted && (
                            <ConnectPayments/>
                        )}
                    </ConnectComponentsProvider>
                )}

                {/* Show error message */}
                {error && <p className="error">Something went wrong!</p>}

                {/* Show additional information */}
                {!loading && (connectedAccountId || accountCreatePending || onboardingExited) && (
                    <div className="dev-callout">
                        {connectedAccountId && <p>Your connected account ID is: <code className="bold">{connectedAccountId}</code></p>}
                        {accountCreatePending && <p>Creating a connected account...</p>}
                        {onboardingExited && <p><center>The Account Onboarding component has exited</center></p>}
                    </div>
                )}
            </div>
        </div>
    );
}

