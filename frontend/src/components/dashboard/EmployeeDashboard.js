import {useEffect, useState} from "react";
import {format} from "date-fns";
import {Button} from "flowbite-react";

export default function EmployeeDashboard() {

    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchAppointments = () => {
        fetch("http://localhost:8080/api/appointment/employee/all", {
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
        fetchAppointments();
    }, []);

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
        <div className="px-24 py-10">
            <ul className="divide-y divide-gray-100">
                {appointments.map((appt) => (
                    <li key={appt.appointmentId} className="flex justify-between gap-x-6 py-5">
                        <div className="flex min-w-0 gap-x-4">
                            <img alt="" src={appt.imageUrl} className="size-12 flex-none rounded-full bg-gray-50" />
                            <div className="min-w-0 flex-auto">
                                <p className="text-sm/6 font-semibold text-gray-900">{appt.customerName}</p>
                                <p className="mt-1 truncate text-xs/5 text-gray-500">{appt.serviceName}</p>
                            </div>
                        </div>
                        <div className="hidden shrink-0 sm:flex sm:flex-col sm:items-end">
                            <p className="mt-1 text-xs/5 text-gray-500">
                                Scheduled for <time dateTime={appt.appointmentDate}>{format(appt.appointmentDate,'MMMM do yyyy, h:mm a')}</time>
                            </p>
                            <div className="mt-1 flex items-center gap-x-1.5">
                                <div className="flex-none rounded-full bg-emerald-500/20 p-1">
                                    <div
                                        className={`size-1.5 rounded-full ${
                                            appt.status === "APPROVED"
                                                ? "bg-emerald-500"
                                                : appt.status === "CANCELLED"
                                                    ? "bg-red-500"
                                                    : "bg-gray-600"
                                        }`}
                                    />
                                </div>
                                <p className="text-xs/5 text-gray-500">{appt.status}</p>
                            </div>
                            <Button.Group outline="true" className="flex flex-col">
                                <Button color="gray" size="xs">
                                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="size-6">
                                        <path strokeLinecap="round" strokeLinejoin="round" d="m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L10.582 16.07a4.5 4.5 0 0 1-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 0 1 1.13-1.897l8.932-8.931Zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0 1 15.75 21H5.25A2.25 2.25 0 0 1 3 18.75V8.25A2.25 2.25 0 0 1 5.25 6H10" />
                                    </svg>
                                </Button>
                                {(appt.status !== "CANCELLED" && new Date(appt.appointmentDate) > new Date()) && (
                                    <Button color="red" size="xs" onClick={() => cancelAppointment(appt.appointmentId)}>
                                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="size-6">
                                            <path strokeLinecap="round" strokeLinejoin="round" d="m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0" />
                                        </svg>
                                    </Button>
                                )}
                            </Button.Group>
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    )
}