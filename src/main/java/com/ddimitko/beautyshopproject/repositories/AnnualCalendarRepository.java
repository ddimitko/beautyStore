package com.ddimitko.beautyshopproject.repositories;

import com.ddimitko.beautyshopproject.entities.calendar.AnnualCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnualCalendarRepository extends JpaRepository<AnnualCalendar, Long> {

    @Query("SELECT ac FROM AnnualCalendar ac WHERE ac.employee.user.id = :userId AND ac.year = :year")
    Optional<AnnualCalendar> findByEmployeeAndYear(@Param("userId") long userId, int year);
}
