package com.ddimitko.beautyshopproject.services;

import com.ddimitko.beautyshopproject.Dto.requests.AppointmentRequestDto;
import com.ddimitko.beautyshopproject.Dto.calendar.TimeSlotDto;
import com.ddimitko.beautyshopproject.configs.sockets.AppointmentWebSocketConfig;
import com.ddimitko.beautyshopproject.entities.*;
import com.ddimitko.beautyshopproject.entities.calendar.DailySchedule;
import com.ddimitko.beautyshopproject.nomenclatures.AppointmentStatus;
import com.ddimitko.beautyshopproject.repositories.*;
import com.ddimitko.beautyshopproject.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.threeten.extra.Interval;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AppointmentService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final AppointmentRepository appointmentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final CalendarService calendarService;
    private final AppointmentWebSocketConfig appointmentWebSocketConfig;
    private final NotificationService notificationService;

    public AppointmentService(RedisTemplate<String, Object> redisTemplate, AppointmentRepository appointmentRepository, EmployeeRepository employeeRepository, UserRepository userRepository, ServiceRepository serviceRepository, CalendarService calendarService, AppointmentWebSocketConfig appointmentWebSocketConfig, NotificationService notificationService) {
        this.redisTemplate = redisTemplate;
        this.appointmentRepository = appointmentRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.calendarService = calendarService;
        this.appointmentWebSocketConfig = appointmentWebSocketConfig;
        this.notificationService = notificationService;
    }

    public List<Appointment> getAllAppointmentsForCustomer(long userId) {
        return appointmentRepository.findAllByCustomerId(userId);
    }

    public List<Appointment> getAllAppointmentsForEmployee(long userId) {
        return appointmentRepository.findAllByEmployeeId(userId);
    }

    public Appointment getAppointmentById(long appointmentId) {
        return appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Appointment not found."));
    }

    private void validateAppointmentRequest(AppointmentRequestDto dto) {
        if (dto.getTimeSlotDto().getDate() == null ||
                dto.getTimeSlotDto().getStartTime() == null ||
                dto.getTimeSlotDto().getEndTime() == null) {
            throw new RuntimeException("Appointment dates and times are required.");
        }
    }

    @Transactional
    public String reserveAppointment(AppointmentRequestDto dto) {
        if (appointmentRepository.isEmployeeAssigningThemselves(dto.getEmployeeId(), dto.getCustomerId())) {
            throw new RuntimeException("An employee cannot book an appointment with themselves.");
        }

        //Generate unique session identifier
        String sessionToken = UUID.randomUUID().toString();

        String redisKey = "reservation:" + sessionToken;

        redisTemplate.opsForValue().set(redisKey, dto, 10, TimeUnit.MINUTES);

        updateTimeSlots(dto.getEmployeeId(), dto.getServiceId(), dto.getTimeSlotDto().getDate());

        return sessionToken; // Return token to frontend
    }

    @Transactional
    public boolean confirmAppointment(String sessionToken) {
        String redisKey = "reservation:" + sessionToken;

        System.out.println(redisKey);

        // Check if session exists
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            return false; // Session expired or invalid
        }

        // Retrieve session details
        AppointmentRequestDto sessionData = (AppointmentRequestDto) redisTemplate.opsForValue().get(redisKey);

        if (sessionData == null) {
            return false; // No data found, possibly expired
        }

        // Remove session after confirmation
        redisTemplate.delete(redisKey);

        addAppointment(sessionData);
        return true;
    }


    @Transactional
    public Appointment addAppointment(AppointmentRequestDto dto) {
        Employee employee = fetchEntity(() -> employeeRepository.findByUserId(dto.getEmployeeId()), "Employee");
        com.ddimitko.beautyshopproject.entities.Service service = fetchEntity(() -> serviceRepository.findById(dto.getServiceId()), "Service");

        validateAppointmentRequest(dto);

        Appointment appointment = new Appointment();

        if (dto.getCustomerId() != null) {
            // Authenticated user
            User user = fetchEntity(() -> userRepository.findById(dto.getCustomerId()), "User");
            if (user == employee.getUser()) {
                throw new RuntimeException("An employee cannot assign themselves to their own appointment.");
            }
            appointment.setCustomer(user);
            appointment.setFullName(user.getFirstName() + " " + user.getLastName());
            appointment.setEmail(user.getEmail());
            appointment.setPhone(user.getPhone());
        } else {
            // Guest user - Store their details directly
            appointment.setFullName(dto.getFullName());
            appointment.setEmail(dto.getEmail());
            appointment.setPhone(dto.getPhone());
        }

        appointment.setEmployee(employee);
        appointment.setAppointmentDate(dto.getTimeSlotDto().getDate());
        appointment.setAppointmentStart(dto.getTimeSlotDto().getStartTime());
        appointment.setAppointmentEnd(dto.getTimeSlotDto().getEndTime());
        appointment.setService(service);
        appointment.setStatus(AppointmentStatus.APPROVED);

        notificationService.sendAppointmentNotification(appointment.getEmployee().getUser().getId(),
                appointment.getFullName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentStart());

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void cancelReservation(String sessionToken) {
        String redisKey = "reservation:" + sessionToken;
        AppointmentRequestDto sessionData = (AppointmentRequestDto) redisTemplate.opsForValue().get(redisKey);
        if (sessionData != null) {
            redisTemplate.delete(redisKey);
            updateTimeSlots(sessionData.getEmployeeId(), sessionData.getServiceId(), sessionData.getTimeSlotDto().getDate());
        }
    }


    public List<Appointment> getAllAppointmentsForGivenDate(LocalDate date, String userId) {
        return appointmentRepository.findAllByAppointmentDateAndEmployee(date, Long.parseLong(userId));
    }

    @Transactional
    public void updateAppointment(AppointmentRequestDto appointmentRequestDto) {

        //TODO: Add checks to avoid conflict between existing appointments, employees on an annual leave and making an appointment outside schedule
        //calendarService.findDailyScheduleByEmployeeAndGivenDate(appointmentRequestDto.getEmployeeId(), appointmentRequestDto.getTimeSlotDto().getDate());

        Appointment updatedAppointment = appointmentRepository.findById(appointmentRequestDto.getAppointmentId()).orElseThrow(() -> new RuntimeException("Appointment not found."));
        updatedAppointment.setAppointmentDate(appointmentRequestDto.getTimeSlotDto().getDate());
        updatedAppointment.setAppointmentStart(appointmentRequestDto.getTimeSlotDto().getStartTime());
        updatedAppointment.setAppointmentEnd(appointmentRequestDto.getTimeSlotDto().getEndTime());
        appointmentRepository.save(updatedAppointment);
    }

    public void deleteAppointmentById(long appointmentId) {
        appointmentRepository.deleteById(appointmentId);
    }

    List<Interval> generateAvailableIntervals(LocalDate date, DailySchedule schedule, int durationInMinutes) {
        ZonedDateTime zonedStart = ZonedDateTime.of(date, schedule.getStartTime(), ZoneId.systemDefault());
        ZonedDateTime zonedEnd = ZonedDateTime.of(date, schedule.getEndTime(), ZoneId.systemDefault());

        if (date.isEqual(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            LocalTime nextAvailableStart = now.plusMinutes(durationInMinutes - (now.getMinute() % durationInMinutes)); // Round up

            // Ensure we don't start after the working hours
            if (nextAvailableStart.isBefore(schedule.getEndTime())) {
                zonedStart = ZonedDateTime.of(date, nextAvailableStart, ZoneId.systemDefault());
            } else {
                return Collections.emptyList(); // No available slots remaining for today
            }
        }

        return Stream.iterate(zonedStart, start -> start.plusMinutes(durationInMinutes))
                .takeWhile(start -> !start.plusMinutes(durationInMinutes).isAfter(zonedEnd)) // Include last slot if valid
                .map(start -> Interval.of(start.toInstant(), start.plusMinutes(durationInMinutes).toInstant()))
                .collect(Collectors.toList());
    }


    public List<TimeSlotDto> getAvailableTimeSlots(LocalDate date, long userId, int serviceId) {
        // Fetch service and schedule
        com.ddimitko.beautyshopproject.entities.Service service = fetchEntity(() -> serviceRepository.findById(serviceId), "Service");
        DailySchedule schedule = calendarService.findDailyScheduleByEmployeeAndGivenDate(userId, date);

        // Ensure the date is valid
        if (date.isBefore(LocalDate.now())) {
            throw new RuntimeException("Appointment date is in the past.");
        }

        // Generate all possible intervals
        List<Interval> availableIntervals = generateAvailableIntervals(date, schedule, service.getDurationInMinutes());

        // Fetch confirmed (not canceled) appointments
        List<Appointment> appointments = appointmentRepository.findAllByAppointmentDateAndEmployee(date, userId)
                .stream()
                .filter(appt -> !appt.getStatus().equals(AppointmentStatus.CANCELLED)) // Exclude canceled appointments
                .toList();

        // Fetch reserved slots from Redis
        Set<String> allReservationKeys = redisTemplate.keys("reservation:*");
        Set<Interval> reservedIntervals = parseRedisKeyToInterval(allReservationKeys, userId, date);


        // Combine confirmed appointments and reserved slots into occupied intervals
        Set<Interval> occupiedIntervals = Stream.concat(
                appointments.stream().map(appt -> Interval.of(
                        ZonedDateTime.of(date, appt.getAppointmentStart(), ZoneId.systemDefault()).toInstant(),
                        ZonedDateTime.of(date, appt.getAppointmentEnd(), ZoneId.systemDefault()).toInstant())),
                reservedIntervals.stream()
        ).collect(Collectors.toSet());

        // Remove occupied intervals from available intervals
        availableIntervals.removeIf(interval ->
                occupiedIntervals.stream().anyMatch(interval::overlaps));

        // Convert available intervals to TimeSlotDto
        return availableIntervals.stream()
                .map(slot -> new TimeSlotDto(
                        LocalTime.ofInstant(slot.getStart(), ZoneId.systemDefault()),
                        LocalTime.ofInstant(slot.getEnd(), ZoneId.systemDefault()),
                        date))
                .toList();
    }

    public void updateTimeSlots(Long employeeId, int serviceId, LocalDate date) {
        List<TimeSlotDto> updatedSlots = getAvailableTimeSlots(date, employeeId, serviceId);
        appointmentWebSocketConfig.sendUpdatedTimeSlots(employeeId, serviceId, date, updatedSlots);
    }

    private Set<Interval> parseRedisKeyToInterval (Set<String> reservationKeys, long userId, LocalDate date) {
        if (reservationKeys == null || reservationKeys.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Interval> reservedIntervals = new HashSet<>();

        for (String key : reservationKeys) {
            AppointmentRequestDto sessionData = (AppointmentRequestDto) redisTemplate.opsForValue().get(key);

            // Ensure data exists and belongs to the correct employee & date
            if (sessionData == null) continue;
            if(!Objects.equals(sessionData.getEmployeeId(), userId)) continue;
            if(!Objects.equals(sessionData.getTimeSlotDto().getDate(), date)) continue;

            // Parse start & end time
            LocalTime startTime = sessionData.getTimeSlotDto().getStartTime();
            LocalTime endTime = sessionData.getTimeSlotDto().getEndTime();

            Instant start = ZonedDateTime.of(date, startTime, ZoneId.systemDefault()).toInstant();
            Instant end = ZonedDateTime.of(date, endTime, ZoneId.systemDefault()).toInstant();

            reservedIntervals.add(Interval.of(start, end));
        }

        return reservedIntervals;
    }


    private <T> T fetchEntity(Supplier<Optional<T>> supplier, String entityName ) {
        return supplier.get().orElseThrow(() -> new RuntimeException(entityName + " not found."));
    }

    public void cancelAppointment(Long appointmentId) {
        Appointment cancelAppointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Appointment not found."));
        cancelAppointment.setStatus(AppointmentStatus.CANCELLED);
        updateTimeSlots(cancelAppointment.getEmployee().getId(), cancelAppointment.getService().getId(), cancelAppointment.getAppointmentDate());
        appointmentRepository.save(cancelAppointment);
    }

}