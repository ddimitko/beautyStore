package com.ddimitko.beautyshopproject.controllers;

import com.ddimitko.beautyshopproject.Dto.calendar.WeeklyScheduleDto;
import com.ddimitko.beautyshopproject.services.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class ScheduleController {

    private final CalendarService calendarService;

    public ScheduleController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    /*@GetMapping("/empl/{id}")
    public WeeklyScheduleDto getScheduleByEmployeeId(@PathVariable long id) {
        return scheduleService.getWeeklySchedule(id);
    }

    //TODO: Prevent ID from changing if schedule exists.
    @PutMapping("/empl/{id}/update")
    public WeeklyScheduleDto saveWeeklySchedule(@PathVariable long id, @RequestBody WeeklyScheduleDto weeklyScheduleDto){
        return scheduleService.saveWeeklySchedule(id, weeklyScheduleDto);
    }*/

    @PostMapping
    public ResponseEntity<String> generateCalendar(@RequestParam long employeeId, @RequestParam int year, @RequestBody WeeklyScheduleDto scheduleDto) {
        calendarService.generateAnnualCalendar(employeeId, year, scheduleDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/days")
    public ResponseEntity<List<LocalDate>> getAvailableDaysForEmployeeForMonth(@RequestParam String employeeId, @RequestParam String year, @RequestParam String month) {
        List<LocalDate> availableDays = calendarService.availableDatesForEmployeeForMonth(Long.parseLong(employeeId), Integer.parseInt(year), Integer.parseInt(month));
        return ResponseEntity.ok(availableDays);
    }
}
