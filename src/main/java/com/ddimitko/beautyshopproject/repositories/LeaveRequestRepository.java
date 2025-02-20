package com.ddimitko.beautyshopproject.repositories;

import com.ddimitko.beautyshopproject.entities.calendar.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @Query("SELECT lr FROM LeaveRequest lr WHERE (EXTRACT(YEAR FROM lr.startDate) = :year OR EXTRACT(YEAR FROM lr.endDate) = :year) AND lr.employee.user.id = :userId")
    List<LeaveRequest> findAllByYearAndEmployee(@Param("year") int year, @Param("userId") long userId);



}
