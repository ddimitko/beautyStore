package com.ddimitko.beautyshopproject.controllers;

import com.ddimitko.beautyshopproject.Dto.requests.AppointmentRequestDto;
import com.ddimitko.beautyshopproject.Dto.responses.AppointmentResponseDto;
import com.ddimitko.beautyshopproject.Dto.calendar.TimeSlotDto;
import com.ddimitko.beautyshopproject.configs.security.CustomUserDetails;
import com.ddimitko.beautyshopproject.entities.Appointment;
import com.ddimitko.beautyshopproject.mappers.AppointmentMapper;
import com.ddimitko.beautyshopproject.services.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;

    public AppointmentController(AppointmentService appointmentService, AppointmentMapper appointmentMapper) {
        this.appointmentService = appointmentService;
        this.appointmentMapper = appointmentMapper;
    }

    @GetMapping("/{id}")
    public AppointmentResponseDto getAppointmentById(@PathVariable int id) {
        return appointmentMapper.mapToResponseDto(appointmentService.getAppointmentById(id));
    }

    @GetMapping("/shop")
    public List<AppointmentResponseDto> getAllAppointmentsForGivenDate(@RequestParam String date, @RequestParam String employeeId) {
        List<Appointment> appointmentList = appointmentService.getAllAppointmentsForGivenDate(LocalDate.parse(date), employeeId);
        List<AppointmentResponseDto> responseList = new LinkedList<>();
        for (Appointment appointment : appointmentList) {
            responseList.add(appointmentMapper.mapToResponseDto(appointment));
        }
        return responseList;
    }

    @GetMapping("/all")
    public ResponseEntity<List<AppointmentResponseDto>> getAllAppointmentsForCustomer(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if( userDetails == null ) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Appointment> appointmentList = appointmentService.getAllAppointmentsForCustomer(userDetails.getUserId());
        List<AppointmentResponseDto> responseList = new LinkedList<>();
        for (Appointment appointment : appointmentList) {
            responseList.add(appointmentMapper.mapToResponseDto(appointment));
        }
        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    @GetMapping("/employee/all")
    public ResponseEntity<List<AppointmentResponseDto>> getAllAppointmentsForEmployee(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if( userDetails == null ) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Appointment> appointmentList = appointmentService.getAllAppointmentsForEmployee(userDetails.getUserId());
        List<AppointmentResponseDto> responseList = new LinkedList<>();
        for (Appointment appointment : appointmentList) {
            responseList.add(appointmentMapper.mapToResponseDto(appointment));
        }
        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    @GetMapping("/availability")
    public ResponseEntity<List<TimeSlotDto>> getAllTimeSlotsForGivenDate(@RequestParam String date, @RequestParam String employeeId, @RequestParam String serviceId) {
        List<TimeSlotDto> timeSlots = appointmentService.getAvailableTimeSlots(LocalDate.parse(date), Long.parseLong(employeeId), Integer.parseInt(serviceId));
        return ResponseEntity.ok(timeSlots);
    }

    /**
     * Step 1: Reserve an appointment slot
     */
    @PostMapping("/reserve")
    public ResponseEntity<String> reserveAppointment(@RequestBody AppointmentRequestDto dto) {
        String sessionToken = appointmentService.reserveAppointment(dto);

        if (sessionToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Time slot already reserved.");
        }

        return ResponseEntity.ok(sessionToken);
    }

    /**
     * Step 2: Confirm the reservation and create the appointment
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmAppointment(@RequestHeader(value = "Authorization", required = false) String sessionToken) {

        if (sessionToken == null || sessionToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session token is missing.");
        }

        // Remove "Bearer " prefix if present
        sessionToken = sessionToken.replace("Bearer ", "").trim();

        boolean confirmed = appointmentService.confirmAppointment(sessionToken);

        if (!confirmed) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Failed to confirm appointment. Reservation might be expired or already booked.");
        }
        else {
            return ResponseEntity.ok().build();
        }
    }

    @PutMapping("/cancel")
    public ResponseEntity<Void> cancelAppointment(@RequestParam Long appointmentId) {
        appointmentService.cancelAppointment(appointmentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reservation/cancel/{sessionToken}")
    public ResponseEntity<?> cancelReservation(@PathVariable String sessionToken) {
        appointmentService.cancelReservation(sessionToken);
        return ResponseEntity.ok("Reservation cancelled successfully.");
    }

    @PutMapping("/update")
    public ResponseEntity<AppointmentResponseDto> updateAppointment(@RequestBody AppointmentRequestDto appointmentRequestDto) {
        return ResponseEntity.ok().build();
    }
}
