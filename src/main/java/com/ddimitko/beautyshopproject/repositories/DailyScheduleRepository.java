package com.ddimitko.beautyshopproject.repositories;

import com.ddimitko.beautyshopproject.entities.calendar.DailySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface DailyScheduleRepository extends JpaRepository<DailySchedule, Long> {

    DailySchedule findDailyScheduleByAnnualCalendarIdAndDate(long annualCalendarId, LocalDate date);

}
