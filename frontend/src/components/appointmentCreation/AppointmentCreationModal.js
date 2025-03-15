import { Stepper } from 'primereact/stepper'
import { StepperPanel } from "primereact/stepperpanel";
import React, {useContext, useState, useRef, useEffect, useMemo} from "react";
import {Dialog} from "@headlessui/react";
import { FormControl, InputLabel, Select, MenuItem, TextField } from "@mui/material";
import { DatePicker, LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { AuthContext } from "../AuthContext";
import AppointmentCreation3 from "./AppointmentCreation3";
import { format } from "date-fns";
import "primereact/resources/themes/lara-light-indigo/theme.css";
import SelectPaymentMethod from "./SelectPaymentMethod";
import {X} from "lucide-react";
import useWebSocket from "../hooks/useWebSocket";
import dayjs from "dayjs";
import { Button } from "flowbite-react";

function AppointmentCreationModal({ isOpen, onClose, shop }) {
    const { isAuthenticated, user } = useContext(AuthContext);
    const stepperRef = useRef(null);

    // Step 1: Employee, Service, Date & Time Selection
    const [employee, setEmployee] = useState(null);
    const [serviceList, setServiceList] = useState([]);
    const [service, setService] = useState(null);
    // Local state for date picker and available dates
    const [datePickerOpen, setDatePickerOpen] = useState(false);
    const [availableDates, setAvailableDates] = useState([]);
    const [selectedMonth, setSelectedMonth] = useState(new Date());
    const [selectedDate, setSelectedDate] = useState(null);
    const [timeSlots, setTimeSlots] = useState([]);
    const [timeSlot, setTimeSlot] = useState(null);

    const [appointmentData, setAppointmentData] = useState(null);
    const [sessionToken, setSessionToken] = useState(null);

    const shouldSubscribe = Boolean(selectedDate && employee && service);

    const topic = useMemo(() => {
        return shouldSubscribe
            ? `/topic/timeSlots/${employee?.userId}/${service?.id}/${dayjs(selectedDate).format('YYYY-MM-DD')}`
            : null;
    }, [selectedDate, employee, service]);

    useWebSocket(
        "http://localhost:8080/ws",
        topic,
        (updatedSlots) => setTimeSlots(updatedSlots),
        shouldSubscribe
    );

    // Step 2: Account Details
    const [accountDetails, setAccountDetails] = useState({
        name: isAuthenticated ? user.firstName + " " + user.lastName : "",
        email: isAuthenticated ? user.email : "",
        phone: isAuthenticated ? user.phone : "",
    });

    // Fetch services when an employee is selected
    useEffect(() => {
        if (employee) {
            setServiceList(employee.serviceList);
        } else {
            setServiceList([]);
        }
    }, [employee]);

    // Handle Employee Selection
    const handleChangeEmployee = (event) => {
        const selectedEmployeeId = event.target.value;
        const selectedEmployee = shop.employeeList.find(emp => emp.userId === parseInt(selectedEmployeeId, 10));
        setEmployee(selectedEmployee);
    };

    // Handle Service Selection
    const handleChangeService = (event) => {
        const selectedServiceId = event.target.value;
        const selectedService = serviceList.find(service => service.id === parseInt(selectedServiceId, 10));
        setService(selectedService);
    };

    // Fetch available dates for the selected month/employee
    const fetchAvailableDates = async (year, month) => {
        try {
            const params = new URLSearchParams({
                employeeId: employee?.userId, // Replace with actual employee id
                year: year,
                month: month + 1, // Convert 0-based month index to 1-based
            });

            const response = await fetch(
                `http://localhost:8080/api/calendar/days?${params.toString()}`,
                {
                    method: 'GET',
                }
            );

            if (!response.ok) {
                console.error('Failed to fetch available dates');
                return;
            }

            const data = await response.json();
            if (Array.isArray(data)) {
                setAvailableDates(data); // Array of 'YYYY-MM-DD' strings
                console.log('Available Dates:', data);
            } else {
                console.error('Fetched data is not an array:', data);
            }
        } catch (error) {
            console.error('Error fetching available dates:', error);
        }
    };

    // Fetch available dates whenever the picker opens or the month changes
    useEffect(() => {
        (async () => {
            if (datePickerOpen) {
                const year = selectedMonth.getFullYear();
                const month = selectedMonth.getMonth();
                await fetchAvailableDates(year, month);
            }
        })();
    }, [selectedMonth, datePickerOpen]);

    // Determines whether a date should be enabled (available) in the date picker
    const isDateAvailable = (date) => {
        if (!Array.isArray(availableDates)) {
            console.error('availableDates is not an array:', availableDates);
            return false;
        }
        if (availableDates.length === 0) return false;

        const today = new Date();
        today.setHours(0, 0, 0, 0);

        // Convert the date to YYYY-MM-DD format
        const dateString = format(new Date(date), 'yyyy-MM-dd');
        // Do not allow past dates
        if (new Date(date) < today) {
            return false;
        }
        // Enable date only if it's in the availableDates list
        return availableDates.includes(dateString);
    };

    // Fetch available time slots for the selected date
    const fetchAvailableSlots = async (date) => {
        if (!date || !employee || !service) {
            return;
        }
        if (!isDateAvailable(date)) {
            console.log('Selected Date is unavailable.');
            return;
        }
        try {
            const formattedDate = format(new Date(date), 'yyyy-MM-dd');
            console.log('Selected Date:', formattedDate);
            const params = new URLSearchParams({
                date: formattedDate,
                employeeId: employee?.userId,
                serviceId: service?.id,
            });
            console.log('API Request Params:', params.toString());
            const response = await fetch(
                `http://localhost:8080/api/appointment/availability?${params.toString()}`,
                {
                    method: 'GET',
                }
            );
            if (!response.ok) {
                throw new Error('Error fetching available slots');
            }
            const data = await response.json();
            setTimeSlot(null);

            setTimeSlots(data);
        } catch (error) {
            console.error('Error fetching available slots:', error);
        }
    };

    // Handle changes to the selected date and fetch time slots
    const handleDateChangeInternal = async (newDate) => {
        setSelectedDate(newDate);
        await fetchAvailableSlots(newDate);
    };

    // Handle changes to the displayed month in the date picker
    const handleMonthChange = (newDate) => {
        if (newDate && typeof newDate.toDate === 'function') {
            const nativeDate = newDate.toDate();
            setSelectedMonth(nativeDate);
        } else if (newDate instanceof Date) {
            setSelectedMonth(newDate);
        } else {
            console.error('Invalid date passed to handleMonthChange:', newDate);
        }
    };

    const handleTimeSlotInternal = (selectedTimeSlotStart) => {
        // Convert to string if necessary to match the type
        const selectedTimeSlotObj = timeSlots.find(
            (ts) => ts.startTime.toString() === selectedTimeSlotStart.toString()
        );
        console.log("Selected Time Slot Object:", selectedTimeSlotObj);
        setTimeSlot(selectedTimeSlotObj);
    };

    // Cleanup function to reset the modal values
    const handleModalClose = () => {
        // Reset all selections to their default state
        setEmployee(null);
        setService(null);
        setSelectedDate(null);
        setTimeSlot(null);
        setTimeSlots([]);
        setAccountDetails({ name: '', email: '' });
        setDatePickerOpen(false);
        setAvailableDates([]);
        setSelectedMonth(new Date());
        // Optionally, if your stepper or other fields have state, reset them as needed
        // Finally, call the parent's onClose to close the modal
        cancelReservation();
        onClose();
    };

    // Function to reserve appointment
    const reserveAppointment = async (appointmentData) => {
        try {
            const response = await fetch("http://localhost:8080/api/appointment/reserve", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(appointmentData),
                credentials: "include",
            });

            // Check if the response is successful
            if (!response.ok) {
                throw new Error("Time slot already reserved. Please select another.");
            }

            // Get the session token from the response
            const sessionToken = await response.text();  // The backend returns the sessionToken as plain text

            console.log(sessionToken);
            console.log(sessionToken.toString());

            if (!sessionToken) {
                throw new Error("Failed to create session. Please try again.");
            }

            return sessionToken;

        } catch (error) {
            alert(error.message);
        }
    };

    const cancelReservation = async () => {
        if (!sessionToken) return;

        try {
            await fetch(`http://localhost:8080/api/appointment/reservation/cancel/${sessionToken}`, {
                method: "DELETE",
                credentials: "include",
            });

            //sessionStorage.removeItem("sessionToken"); // Remove locally
            console.log("Reservation cancelled.");
        } catch (error) {
            console.error("Error canceling reservation:", error);
        }
    };

    const handleNext = () => {
        if (!accountDetails.name || !accountDetails.email) {
            alert("Please fill in your name and email.");
            return;
        }

        const newAppointment = {
            employeeId: employee.userId,
            serviceId: service.id,
            timeSlotDto: {
                date: timeSlot.date,
                startTime: timeSlot.startTime,
                endTime: timeSlot.endTime,
            },
        };

        if (isAuthenticated) {
            newAppointment.customerId = user.id;
        }
        newAppointment.fullName = accountDetails.name;
        newAppointment.email = accountDetails.email;
        newAppointment.phone = accountDetails.phone;

        console.log(newAppointment);

        setAppointmentData(newAppointment);
    };

    useEffect(() => {
        if (appointmentData) {
            console.log("Calling reserveAppointment:", appointmentData);
            reserveAppointment(appointmentData).then(
                token => {
                    if(token) {
                        setSessionToken(token);
                        stepperRef.current.nextCallback(appointmentData);
                    }
                }
            );
        }
    }, [appointmentData]); // Runs when `appointmentData` updates

    return (
        <Dialog open={isOpen} onClose={(event) => {
            if (event?.target === event?.currentTarget) return; // Ignore backdrop clicks
            onClose();}} className="relative z-50">
            {/* The overlay to darken the background */}
            <div className="fixed inset-0 bg-black bg-opacity-25" aria-hidden="true" />

            {/* This wrapper centers the modal content */}
            <div className="fixed inset-0 flex items-center justify-center">
                <Dialog.Panel className="w-full max-w-4xl transform overflow-hidden rounded-2xl bg-white p-6 text-left shadow-xl transition-all relative z-50">
                    {/* Close Button (X) in the top-right corner */}
                    <button
                        onClick={handleModalClose}
                        className="absolute top-4 right-4 p-2 rounded-full bg-gray-200 hover:bg-gray-300 transition"
                        aria-label="Close"
                    >
                        <X className="w-5 h-5 text-gray-700" />
                    </button>
                    <Dialog.Title className="text-2xl font-bold mb-4">
                        Book an Appointment
                    </Dialog.Title>

                    <Stepper linear ref={stepperRef} style={{ flexBasis: '50rem' }}>
                        {/* Step 1: Select Employee, Service, Date & Time */}
                        <StepperPanel header="Select Details">
                            <div className="flex flex-column h-12rem">
                                <div className="surface-ground flex-auto flex-col justify-content-center align-items-center font-medium">
                                    <FormControl fullWidth>
                                        <InputLabel>Employee</InputLabel>
                                        <Select
                                            value={employee?.userId || ""}
                                            onChange={handleChangeEmployee}
                                        >
                                            {shop.employeeList.filter(emp => emp.userId !== user?.id) // Exclude logged-in user
                                                .map(emp => (
                                                    <MenuItem key={emp.userId} value={emp.userId}>
                                                        {emp.fullName}
                                                    </MenuItem>
                                                ))}
                                        </Select>
                                    </FormControl>
                                    <FormControl fullWidth disabled={!employee}>
                                        <InputLabel>Service</InputLabel>
                                        <Select
                                            value={service?.id || ""}
                                            onChange={handleChangeService}
                                        >
                                            {serviceList.map((svc) => (
                                                <MenuItem key={svc.id} value={svc.id}>
                                                    {svc.name}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                    <LocalizationProvider dateAdapter={AdapterDayjs}>
                                        <DatePicker value={selectedDate} onChange={handleDateChangeInternal}
                                                    onMonthChange={handleMonthChange}
                                            // Disable dates not available (or in the past)
                                                    shouldDisableDate={(date) => !isDateAvailable(date)}
                                                    onOpen={() => setDatePickerOpen(true)}
                                                    onClose={() => setDatePickerOpen(false)}/>
                                    </LocalizationProvider>
                                    {timeSlots.length > 0 && (
                                        <FormControl fullWidth>
                                            <h3 className="mt-2 text-center">Available Time Slots</h3>
                                            <div className="grid grid-cols-5 gap-2 mt-2">
                                                {timeSlots.map((slot) => (
                                                    <Button
                                                        className={`
                                ${slot.startTime === timeSlot?.startTime ? 'bg-blue-500 text-white' : 'bg-gray-200'}
                                hover:bg-blue-600
                            `}
                                                        key={slot.startTime}
                                                        color="gray"
                                                        onClick={() => handleTimeSlotInternal(slot.startTime)}>
                                                        {slot.startTime} - {slot.endTime}
                                                    </Button>
                                                ))}
                                            </div>
                                        </FormControl>
                                    )}
                                </div>
                            </div>
                            <div className="flex pt-4 justify-end">
                                <Button
                                    label="Next"
                                    icon="pi pi-arrow-right"
                                    iconPos="right"
                                    disabled={!employee || !service || !selectedDate || !timeSlot}
                                    onClick={() => stepperRef.current.nextCallback()}
                                />
                            </div>
                        </StepperPanel>

                        {/* Step 2: Account Details */}
                        <StepperPanel header="Enter Account Details">
                            <div className="flex flex-column h-12rem">
                                <div className="border-2 border-dashed surface-border border-round surface-ground flex-auto flex justify-content-center align-items-center font-medium">
                                    <TextField
                                        fullWidth
                                        label="Full Name"
                                        value={accountDetails.name}
                                        onChange={(e) =>
                                            setAccountDetails({
                                                ...accountDetails,
                                                name: e.target.value,
                                            })
                                        }
                                        disabled={isAuthenticated}
                                        required
                                    />
                                    <TextField
                                        fullWidth
                                        label="Phone Number"
                                        value={accountDetails.phone}
                                        onChange={(e) =>
                                            setAccountDetails({
                                                ...accountDetails,
                                                phone: e.target.value,
                                            })
                                        }
                                        disabled={isAuthenticated}
                                    />
                                    <TextField
                                        fullWidth
                                        label="Email"
                                        type="email"
                                        value={accountDetails.email}
                                        onChange={(e) =>
                                            setAccountDetails({
                                                ...accountDetails,
                                                email: e.target.value,
                                            })
                                        }
                                        required
                                        disabled={isAuthenticated}
                                    />
                                </div>
                            </div>
                            <div className="flex pt-4 justify-between">
                                <Button
                                    label="Back"
                                    severity="secondary"
                                    icon="pi pi-arrow-left"
                                    onClick={() => {// Clear any stored appointment reservation details when going back
                                        cancelReservation();
                                        // Then go back one step in your stepper
                                        stepperRef.current.prevCallback();
                                    }}
                                />
                                <Button
                                    label="Next"
                                    icon="pi pi-arrow-right"
                                    iconPos="right"
                                    disabled={!accountDetails.name || !accountDetails.email}
                                    onClick={handleNext}
                                />
                            </div>
                        </StepperPanel>

                        <StepperPanel header="Payment Method">
                            <div className="flex flex-column h-12rem">
                                <div className="border-2 border-dashed surface-border border-round surface-ground flex-auto flex justify-content-center align-items-center font-medium">
                                    <SelectPaymentMethod shopId={shop.id}
                                                         sessionToken={sessionToken}
                                                         appointmentData={appointmentData}
                                                         onNext={stepperRef}
                                                         onBack={() => stepperRef.current.prevCallback()}
                                    />
                                </div>
                            </div>
                        </StepperPanel>

                        {/* Step 3: Confirmation */}
                        <StepperPanel header="Confirmation">
                            <AppointmentCreation3
                                employee={employee}
                                service={service}
                                timeSlot={timeSlot}
                                accountDetails={accountDetails}
                                onClose={onClose}
                            />
                        </StepperPanel>
                    </Stepper>
                </Dialog.Panel>
            </div>
        </Dialog>
    );

}

export default AppointmentCreationModal;