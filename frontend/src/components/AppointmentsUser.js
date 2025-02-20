import { useEffect, useState } from "react";
import { format } from "date-fns"
import {Button} from "@mui/material";
import {useAuth} from "./AuthContext";
import {Navigate} from "react-router-dom";

export default function AppointmentsUser() {
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const { isAuthenticated } = useAuth();

    const fetchAppointments = () => {
        fetch("http://localhost:8080/api/appointment/all", {
            method: "GET",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
            },
        })
            .then((res) => {
                if (!res.ok) {
                    throw new Error("Failed to fetch appointments");
                }
                return res.json();
            })
            .then((data) => {
                setAppointments(data);
                setLoading(false);
            })
            .catch((err) => {
                console.error("Error fetching appointments:", err);
                setError(err.message);
                setLoading(false);
            });
    };

    useEffect(() => {
        if (!isAuthenticated) return;

        fetchAppointments();
    }, [isAuthenticated]);

    if (!isAuthenticated) {
        return <Navigate to="/" />;
    }

    const cancelAppointment = (appointmentId) => {
        const confirmCancel = window.confirm("Are you sure you want to cancel your appointment?");
        if (!confirmCancel) return; // 🚀 Exit if user cancels

        fetch(`http://localhost:8080/api/appointment/cancel?appointmentId=${appointmentId}`, {
            method: "PUT",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
            },
        })
            .then((res) => {
                if (!res.ok) {
                    throw new Error("Failed to cancel appointment");
                }
                return res.text();
            })
            .then(() => {
                // ✅ Refresh the appointment list after canceling
                fetchAppointments();
            })
            .catch((err) => {
                console.error("Error canceling appointment:", err);
                alert("Failed to cancel appointment. Please try again.");
            });
    };

    if (loading) {
        return <p className="text-center text-gray-500">Loading appointments...</p>;
    }

    if (error) {
        return <p className="text-center text-red-500">Error: {error}</p>;
    }

    if (appointments.length === 0) {
        return <p className="text-center text-gray-500">No Appointments created yet.</p>;
    }

    return (
        <div className="flex flex-wrap items-center justify-center p-6">
        <ul className="divide-y divide-gray-100">
            {appointments.map((appointment) => (
                <li key={appointment.appointmentId} className="flex justify-between gap-x-6 py-5">
                    <div className="flex min-w-0 gap-x-4">
                        <div className="min-w-0 flex-auto">
                            <p className="text-sm font-semibold text-gray-900">{appointment.shopName}</p>
                            <p className="mt-1 truncate text-xs text-gray-500">{appointment.employeeName}</p>
                        </div>
                    </div>
                    <div className="hidden shrink-0 sm:flex sm:flex-col sm:items-end">
                        <p className="text-sm text-gray-900">{appointment.status}</p>
                            <p className="mt-1 text-xs text-gray-500">
                                Scheduled for <time dateTime={appointment.appointmentDate}>{format(appointment.appointmentDate,'MMMM do yyyy, h:mm a')}</time>
                            </p>
                        {appointment.status !== "CANCELLED" && (
                        <Button variant="outlined" size="medium" color="error" onClick={() => cancelAppointment(appointment.appointmentId)}>
                            Cancel
                        </Button>
                            )}
                    </div>
                </li>
            ))}
        </ul>
        </div>
    );
}