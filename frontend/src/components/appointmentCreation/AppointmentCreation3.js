import React, { useState } from "react";
import { Card, CardContent, Typography, Button } from "@mui/material";

function AppointmentCreation3({employee, service, timeSlot, accountDetails, paymentMethod, onClose }) {

    return (
        <Card sx={{ maxWidth: 500, margin: "auto", padding: 2 }}>
            <CardContent>
                <Typography variant="h6">Appointment confirmed</Typography>

                { employee && service && timeSlot && accountDetails && (
                    <>
                <Typography variant="body1"><strong>Employee:</strong> {employee.fullName}</Typography>
                <Typography variant="body1"><strong>Service:</strong> {service.name}</Typography>
                <Typography variant="body1"><strong>Date:</strong> {timeSlot.date}</Typography>
                <Typography variant="body1"><strong>Time:</strong> {timeSlot.startTime} - {timeSlot.endTime}</Typography>
                <Typography variant="body1" sx={{ mt: 2 }}><strong>Customer Name:</strong> {accountDetails.name}</Typography>
                <Typography variant="body1"><strong>Email:</strong> {accountDetails.email}</Typography>
                <Typography variant="body1"><strong>Phone Number:</strong> {accountDetails.phone}</Typography>
                </>
            )}
                <p><strong>Payment Method:</strong> {paymentMethod}</p>

                <Button
                    variant="contained"
                    color="primary"
                    sx={{ mt: 3 }}
                    fullWidth
                    onClick={onClose}
                >
                    Close
                </Button>
            </CardContent>
        </Card>
    );
}

export default AppointmentCreation3;
