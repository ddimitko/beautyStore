import {useState} from "react";
import { FormControl, InputLabel, Select, MenuItem } from "@mui/material";
import { Button } from "primereact/button";
import Checkout from "../Checkout";

function SelectPaymentMethod({ sessionToken, appointmentData, onNext, onBack, shopId}) {
    const [paymentMethod, setPaymentMethod] = useState("");
    const [paymentCompleted, setPaymentCompleted] = useState(false);
    const [connectedAccountId, setConnectedAccountId] = useState(null);
    const [clientSecret, setClientSecret] = useState(null);

    const handleChangePayment = async (event) => {
        setPaymentMethod(event.target.value);

        if (event.target.value === "credit_card") {
            await handleCardPayment(); // Fetch the clientSecret first
        }
    };

    const handleCashPayment = async () => {
        const response = await fetch("http://localhost:8080/api/payments/confirm", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ sessionToken }),
        });

        const data = await response.json();
        if (data.success) {
            setPaymentCompleted(true);
        }
    };

    const handleCardPayment = async () => {
        try {
            const response = await fetch(`http://localhost:8080/api/payments/create-checkout-session`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({
                    sessionToken,
                    customerId: appointmentData.customerId || null,
                    customerName: appointmentData.fullName,
                    customerEmail: appointmentData.email,
                    serviceId: appointmentData.serviceId,
                    shopId: shopId,
                }),
            });

            const data = await response.json();

            if (data.clientSecret && data.connectedAccountId) {
                setConnectedAccountId(data.connectedAccountId);
                setClientSecret(data.clientSecret);
            } else {
                console.error("Failed to fetch client secret:", data.error);
            }
        } catch (error) {
            console.error("Error fetching checkout session:", error);
        }
    };

    if (paymentCompleted) {
        return <h2>Appointment Confirmed! 🎉</h2>;
    }

    return (
        <div>
            <h2>Select a Payment Method</h2>
            <FormControl fullWidth>
                <InputLabel>Payment Method</InputLabel>
                <Select value={paymentMethod} onChange={handleChangePayment}>
                    <MenuItem value="credit_card">Credit/Debit Card</MenuItem>
                    <MenuItem value="cash">Cash</MenuItem>
                </Select>
            </FormControl>

            <div className="flex pt-4 justify-between">
                <Button severity="secondary" onClick={onBack}>Back</Button>

                {paymentMethod === "cash" && (
                    <Button onClick={handleCashPayment}>Confirm Booking</Button>
                )}

                {paymentMethod === "credit_card" && sessionToken && clientSecret ? (
                    <div className="flex justify-center">
                    <Checkout connectedAccountId={connectedAccountId} clientSecret={clientSecret} />
                    </div>
                ): (
                    <p>Loading form...</p>
                )}
            </div>
        </div>
    );
}

export default SelectPaymentMethod;
