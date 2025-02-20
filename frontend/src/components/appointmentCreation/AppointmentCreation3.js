import React, { useState } from "react";
import { Card, CardContent, Typography, Button } from "@mui/material";

function AppointmentCreation3({employee, service, timeSlot, accountDetails, paymentMethod, onClose }) {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [confirmationMessage, setConfirmationMessage] = useState("");

    const handleConfirmAppointment = async () => {
        setIsSubmitting(true);
        setConfirmationMessage("");

        const sessionToken = sessionStorage.getItem("sessionToken");

        if (!sessionToken) {
            setConfirmationMessage("Error: Session key is missing. Please log in again.");
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

            setConfirmationMessage("✅ Appointment successfully booked!");
            sessionStorage.removeItem("sessionToken");

            // Close the modal after confirmation
            setTimeout(() => {
                onClose();
            }, 1500);

        } catch (error) {
            setConfirmationMessage(`❌ Error: ${error.message || "Booking failed. Please try again."}`);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Card sx={{ maxWidth: 500, margin: "auto", padding: 2 }}>
            <CardContent>
                <Typography variant="h6">Confirm Appointment</Typography>

                <Typography variant="body1"><strong>Employee:</strong> {employee.fullName}</Typography>
                <Typography variant="body1"><strong>Service:</strong> {service.name}</Typography>
                <Typography variant="body1"><strong>Date:</strong> {timeSlot.date}</Typography>
                <Typography variant="body1"><strong>Time:</strong> {timeSlot.startTime} - {timeSlot.endTime}</Typography>

                <Typography variant="body1" sx={{ mt: 2 }}><strong>Customer Name:</strong> {accountDetails.name}</Typography>
                <Typography variant="body1"><strong>Email:</strong> {accountDetails.email}</Typography>
                <Typography variant="body1"><strong>Phone Number:</strong> {accountDetails.phone}</Typography>
                <p><strong>Payment Method:</strong> {paymentMethod}</p>

                {confirmationMessage && (
                    <Typography variant="body2" sx={{ color: confirmationMessage.includes("successfully") ? "green" : "red", mt: 2 }}>
                        {confirmationMessage}
                    </Typography>
                )}

                <Button
                    variant="contained"
                    color="primary"
                    sx={{ mt: 3 }}
                    fullWidth
                    disabled={isSubmitting}
                    onClick={handleConfirmAppointment}
                >
                    {isSubmitting ? "Booking..." : "Confirm Booking"}
                </Button>
            </CardContent>
        </Card>
    );
}

export default AppointmentCreation3;
