import React, { useState, useEffect } from "react";
import { useStripeConnect } from "../hooks/useStripeConnect";
import {
    ConnectAccountOnboarding,
    ConnectComponentsProvider,
} from "@stripe/react-connect-js";
import {useLocation} from "react-router-dom";

export default function PaymentOnboarding() {
    const [accountCreatePending, setAccountCreatePending] = useState(false);
    const [onboardingExited, setOnboardingExited] = useState(false);
    const [error, setError] = useState(false);
    const [connectedAccountId, setConnectedAccountId] = useState(null);
    const stripeConnectInstance = useStripeConnect(connectedAccountId);

    const location = useLocation(); // Use useLocation to get the state
    const { shopId } = location.state || {}; // Destructure the shopId from the state, fallback to empty object

    // Fetch the connected account ID from the backend when the component loads
    useEffect(() => {
        console.log(shopId)
        fetch(`http://localhost:8080/api/payments/account?shopId=${shopId}`, {
            method: "GET",
            credentials: "include"
        })
            .then((response) => response.json())
            .then((data) => {
                console.log(data)
                if (data.account) {
                    setConnectedAccountId(data.account);
                }
            })
            .catch((err) => console.error("Error fetching account ID:", err));
    }, []);

    // Function to create a connected account
    const createConnectedAccount = async () => {
        setAccountCreatePending(true);
        setError(false);

        try {
            const response = await fetch(`http://localhost:8080/api/payments/account?shopId=${shopId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                credentials: "include"
            });
            const data = await response.json();
            console.log(data);

            setAccountCreatePending(false);

            if (data.account) {
                setConnectedAccountId(data.account);
            } else {
                setError(true);
                console.log(error);
            }
        } catch (err) {
            console.error("Error creating account:", err);
            setError(true);
            setAccountCreatePending(false);
        }
    };

    return (
        <div className="container">
            <div className="banner">
                <h2>Rocket Rides</h2>
            </div>
            <div className="content">
                {!connectedAccountId && <h2>Get ready for takeoff</h2>}
                {connectedAccountId && !stripeConnectInstance && (
                    <h2>Add information to start accepting money</h2>
                )}
                {!connectedAccountId && (
                    <p>
                        Rocket Rides is the world's leading air travel platform: join our
                        team of pilots to help people travel faster.
                    </p>
                )}
                {!accountCreatePending && !connectedAccountId && (
                    <div>
                        <button onClick={createConnectedAccount}>Sign up</button>
                    </div>
                )}
                {stripeConnectInstance && (
                    <ConnectComponentsProvider connectInstance={stripeConnectInstance}>
                        <ConnectAccountOnboarding
                            onExit={() => setOnboardingExited(true)}
                        />
                    </ConnectComponentsProvider>
                )}
                {error && <p className="error">Something went wrong!</p>}
                {(connectedAccountId || accountCreatePending || onboardingExited) && (
                    <div className="dev-callout">
                        {connectedAccountId && (
                            <p>
                                Your connected account ID is:{" "}
                                <code className="bold">{connectedAccountId}</code>
                            </p>
                        )}
                        {accountCreatePending && <p>Creating a connected account...</p>}
                        {onboardingExited && (
                            <p>The Account Onboarding component has exited</p>
                        )}
                    </div>
                )}
                <div className="info-callout">
                    <p>
                        This is a sample app for Connect onboarding using the Account
                        Onboarding embedded component.{" "}
                        <a
                            href="https://docs.stripe.com/connect/onboarding/quickstart?connect-onboarding-surface=embedded"
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            View docs
                        </a>
                    </p>
                </div>
            </div>
        </div>
    );
}
