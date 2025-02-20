package com.ddimitko.beautyshopproject.services;

import com.ddimitko.beautyshopproject.Dto.requests.AppointmentRequestDto;
import com.ddimitko.beautyshopproject.Dto.calendar.TimeSlotDto;
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
    private final String SLOT_KEY_PREFIX = "reserved-slot:";

    private final AppointmentRepository appointmentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final CalendarService calendarService;

    public AppointmentService(RedisTemplate<String, Object> redisTemplate, AppointmentRepository appointmentRepository, EmployeeRepository employeeRepository, UserRepository userRepository, ServiceRepository serviceRepository, CalendarService calendarService) {
        this.redisTemplate = redisTemplate;
        this.appointmentRepository = appointmentRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.calendarService = calendarService;
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

    private void validateSlot(List<TimeSlotDto> availableSlots, LocalTime start, LocalTime end, LocalDate date) {
        boolean isValidSlot = availableSlots.stream()
                .anyMatch(slot -> slot.getStartTime().equals(start) &&
                        slot.getEndTime().equals(end) &&
                        slot.getDate().equals(date));
        if (!isValidSlot) {
            throw new RuntimeException("Slot is not valid.");
        }
    }

    @Transactional
    public String reserveAppointment(AppointmentRequestDto dto) {
        if (appointmentRepository.isEmployeeAssigningThemselves(dto.getEmployeeId(), dto.getCustomerId())) {
            throw new RuntimeException("An employee cannot book an appointment with themselves.");
        }

        // Generate unique session identifier
        String sessionToken = UUID.randomUUID().toString();

        String redisKey = "reservation:" + sessionToken;

        // Store session details in Redis (expires in 3 minutes)
        Map<String, Object> sessionData = new HashMap<>();
        if(dto.getCustomerId() != null) {
            sessionData.put("customerId", dto.getCustomerId());
        }
        sessionData.put("employeeId", dto.getEmployeeId());
        sessionData.put("email", dto.getEmail());
        sessionData.put("fullName", dto.getFullName());
        sessionData.put("phone", dto.getPhone());
        sessionData.put("serviceId", dto.getServiceId());
        sessionData.put("date", dto.getTimeSlotDto().getDate().toString());
        sessionData.put("startTime", dto.getTimeSlotDto().getStartTime().toString());
        sessionData.put("endTime", dto.getTimeSlotDto().getEndTime().toString());

        redisTemplate.opsForHash().putAll(redisKey, sessionData);
        redisTemplate.expire(redisKey, 3, TimeUnit.MINUTES);

        return sessionToken; // Return token to frontend
    }

    @Transactional
    public boolean confirmAppointment(String sessionToken) {
        String redisKey = "reservation:" + sessionToken;

        // Check if session exists
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            return false; // Session expired or invalid
        }

        // Retrieve session details
        Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(redisKey);
        if (sessionData.isEmpty()) {
            return false; // No data found, possibly expired
        }

        // Convert session data to DTO
        AppointmentRequestDto dto = new AppointmentRequestDto();
        dto.setCustomerId(sessionData.get("customerId") != null ? Long.valueOf(String.valueOf(sessionData.get("customerId"))) : null);
        dto.setEmployeeId(Long.parseLong(String.valueOf(sessionData.get("employeeId"))));
        dto.setServiceId(Integer.parseInt(String.valueOf(sessionData.get("serviceId"))));
        dto.setFullName((String) sessionData.get("fullName"));
        dto.setEmail((String) sessionData.get("email"));
        dto.setPhone((String) sessionData.get("phone"));

        TimeSlotDto timeSlot = new TimeSlotDto();
        timeSlot.setDate(LocalDate.parse((String) sessionData.get("date")));
        timeSlot.setStartTime(LocalTime.parse((String) sessionData.get("startTime")));
        timeSlot.setEndTime(LocalTime.parse((String) sessionData.get("endTime")));
        dto.setTimeSlotDto(timeSlot);

        // Remove session after confirmation
        redisTemplate.delete(redisKey);

        addAppointment(dto);
        return true;
    }


    @Transactional
    public Appointment addAppointment(AppointmentRequestDto dto) {
        Employee employee = fetchEntity(() -> employeeRepository.findByUserId(dto.getEmployeeId()), "Employee");
        com.ddimitko.beautyshopproject.entities.Service service = fetchEntity(() -> serviceRepository.findById(dto.getServiceId()), "Service");

        validateAppointmentRequest(dto);

        //List<TimeSlotDto> availableSlots = getAvailableTimeSlots(dto.getTimeSlotDto().getDate(), employee.getId(), service.getId());
        //System.out.println("Available Slots: " + availableSlots); // Debugging
        //validateSlot(availableSlots, dto.getTimeSlotDto().getStartTime(), dto.getTimeSlotDto().getEndTime(), dto.getTimeSlotDto().getDate());

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

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void cancelReservation(String sessionToken) {
        String redisKey = "reservation:" + sessionToken;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            redisTemplate.delete(redisKey);
        }
    }


    public List<Appointment> getAllAppointmentsForGivenDate(LocalDate date, String userId) {
        //employeeRepository.findById(employeeId).orElseThrow(() -> new RuntimeException("Employee not found."));
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
            zonedStart = ZonedDateTime.of(LocalDate.now(), LocalTime.now(), ZoneId.systemDefault());
        }

        return Stream.iterate(zonedStart, start -> start.plusMinutes(durationInMinutes))
                .takeWhile(start -> start.plusMinutes(durationInMinutes).isBefore(zonedEnd))
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
        Set<String> reservedSlotKeys = redisTemplate.keys(SLOT_KEY_PREFIX + userId + ":" + date + ":*");
        Set<Interval> reservedIntervals = reservedSlotKeys != null ? reservedSlotKeys.stream()
                .map(this::parseRedisKeyToInterval) // Parse Redis key to Interval
                .collect(Collectors.toSet()) : Collections.emptySet();

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
                .collect(Collectors.toList());
    }

    private Interval parseRedisKeyToInterval(String redisKey) {
        // Extract details from Redis key: "reserved-slot:employeeId:date:startHour:startMinute:endHour:endMinute"
        String[] parts = redisKey.split(":");

        if (parts.length < 7) {
            throw new IllegalArgumentException("Invalid Redis key format: " + redisKey);
        }

        LocalDate date = LocalDate.parse(parts[2]); // Date part

        // Properly reconstruct the start and end times
        LocalTime startTime = LocalTime.of(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
        LocalTime endTime = LocalTime.of(Integer.parseInt(parts[5]), Integer.parseInt(parts[6]));

        // Convert to ZonedDateTime
        ZonedDateTime start = ZonedDateTime.of(date, startTime, ZoneId.systemDefault());
        ZonedDateTime end = ZonedDateTime.of(date, endTime, ZoneId.systemDefault());

        return Interval.of(start.toInstant(), end.toInstant());
    }


    private <T> T fetchEntity(Supplier<Optional<T>> supplier, String entityName ) {
        return supplier.get().orElseThrow(() -> new RuntimeException(entityName + " not found."));
    }

    public void cancelAppointment(Long appointmentId) {
        Appointment cancelAppointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Appointment not found."));
        cancelAppointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(cancelAppointment);
    }

}
