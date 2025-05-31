package com.ddimitko.beautyshopproject.services;

import com.ddimitko.beautyshopproject.Dto.calendar.DailyScheduleDto;
import com.ddimitko.beautyshopproject.Dto.calendar.WeeklyScheduleDto;
import com.ddimitko.beautyshopproject.entities.Employee;
import com.ddimitko.beautyshopproject.entities.calendar.AnnualCalendar;
import com.ddimitko.beautyshopproject.entities.calendar.DailySchedule;
import com.ddimitko.beautyshopproject.entities.calendar.LeaveRequest;
import com.ddimitko.beautyshopproject.repositories.AnnualCalendarRepository;
import com.ddimitko.beautyshopproject.repositories.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final AnnualCalendarRepository annualCalendarRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveService leaveService;

    public CalendarService(AnnualCalendarRepository annualCalendarRepository, EmployeeRepository employeeRepository, LeaveService leaveService) {
        this.annualCalendarRepository = annualCalendarRepository;
        this.employeeRepository = employeeRepository;
        this.leaveService = leaveService;
    }

    @Transactional
    public AnnualCalendar generateAnnualCalendar(Long userId, int year, WeeklyScheduleDto request) {

        if(annualCalendarRepository.findByEmployeeAndYear(userId, year).isPresent()) {
            throw new RuntimeException("Annual Calendar already generated for employee: " + userId + ", year: " + year);
        }
        else{
            Employee employee = employeeRepository.findByUserId(userId).orElseThrow(()-> new RuntimeException("Employee not found"));
            AnnualCalendar annualCalendar = new AnnualCalendar();
            annualCalendar.setYear(year);
            annualCalendar.setEmployee(employee);

            List<DailySchedule> calendarDays = new ArrayList<>();
            List<DailyScheduleDto> dailySchedules = request.getDailySchedules();

            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);

            while(!startDate.isAfter(endDate)) {
                DayOfWeek currentDay = startDate.getDayOfWeek();

                DailyScheduleDto matchingSchedule = dailySchedules.stream()
                        .filter(schedule -> DayOfWeek.valueOf(schedule.getDayOfWeek()) == currentDay)
                        .findFirst().orElse(null);

                if(matchingSchedule != null) {

                    //Create a DailySchedule for this specific date
                    DailySchedule dailySchedule = new DailySchedule();
                    dailySchedule.setDayOfWeek(currentDay);
                    dailySchedule.setStartTime(matchingSchedule.getStartTime());
                    dailySchedule.setEndTime(matchingSchedule.getEndTime());
                    dailySchedule.setDate(startDate);
                    dailySchedule.setAnnualCalendar(annualCalendar);

                    calendarDays.add(dailySchedule);
                }

                startDate = startDate.plusDays(1); //Move to the next day
            }

            annualCalendar.setWeeklySchedule(calendarDays);
            return annualCalendarRepository.save(annualCalendar);
        }
    }

    @Cacheable(value = "annualCalendars", key = "#employeeId + '-' + #year")
    public AnnualCalendar findAnnualByEmployeeForYear(long employeeId, int year) {
        return annualCalendarRepository.findByEmployeeAndYear(employeeId, year)
                .orElseThrow(() -> new RuntimeException("Calendar not generated or employee with id: " + employeeId + " not found"));
    }


    public DailySchedule findDailyScheduleByEmployeeAndGivenDate(long userId, LocalDate date) {
        AnnualCalendar annual = findAnnualByEmployeeForYear(userId, date.getYear());
        List<LeaveRequest> approvedLeaves = leaveService.getApprovedLeavesByEmployee(userId, date.getYear());

        //Check if employee is on annual leave
        if(!approvedLeaves.isEmpty()) {
            for(LeaveRequest leaveRequest : approvedLeaves) {
                if(!date.isAfter(leaveRequest.getEndDate()) && !date.isBefore(leaveRequest.getStartDate())) {
                    throw new RuntimeException("Employee is on leave");
                }
            }
        }

        return annual.getWeeklySchedule()
                .stream().filter(schedule -> schedule.getDate()
                        .equals(date)).findAny()
                .orElseThrow(() -> new RuntimeException("Daily schedule not found or employee is OFF for that day."));
    }

    public List<LocalDate> availableDatesForEmployeeForMonth(long userId, int year, int month) {

        List<LeaveRequest> approvedLeaves = leaveService.getApprovedLeavesByEmployee(userId, year);

        AnnualCalendar annualCalendar = findAnnualByEmployeeForYear(userId, year);
        List<DailySchedule> employeeScheduleForMonth = annualCalendar.getWeeklySchedule().stream()
                .filter(schedule -> schedule.getDate().getMonthValue() == month)
                .toList();

        // Step 3: Filter out dates the employee is on leave
        return employeeScheduleForMonth.stream()
                .map(DailySchedule::getDate)
                .filter(date -> !isEmployeeOnLeave(date, approvedLeaves))
                .collect(Collectors.toList());

    }

    private boolean isEmployeeOnLeave(LocalDate date, List<LeaveRequest> approvedLeaves) {
        for (LeaveRequest leaveRequest : approvedLeaves) {
            if ((date.isEqual(leaveRequest.getStartDate()) || date.isAfter(leaveRequest.getStartDate())) &&
                    (date.isEqual(leaveRequest.getEndDate()) || date.isBefore(leaveRequest.getEndDate()))) {
                return true; // Employee is on leave for this date
            }
        }
        return false; // Employee is not on leave for this date
    }
}
