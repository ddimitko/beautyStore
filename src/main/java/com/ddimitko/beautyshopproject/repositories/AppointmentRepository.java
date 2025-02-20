package com.ddimitko.beautyshopproject.repositories;

import com.ddimitko.beautyshopproject.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByCustomerId(long userId);

    @Query("SELECT a FROM Appointment a WHERE a.employee.id = (SELECT e.id FROM Employee e WHERE e.user.id = :userId)")
    List<Appointment> findAllByEmployeeId(long userId);
    @Query("SELECT a FROM Appointment a JOIN a.employee e WHERE a.appointmentDate = :appointmentDate AND e.user.id = :userId")
    List<Appointment> findAllByAppointmentDateAndEmployee(@Param("appointmentDate") LocalDate appointmentDate, @Param("userId") long userId);
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.id = :employeeId AND e.user.id = :customerId")
    boolean isEmployeeAssigningThemselves(@Param("employeeId") Long employeeId, @Param("customerId") Long customerId);

    Appointment getAppointmentByAppointmentDateAndAppointmentStartAndEmployeeId(LocalDate appointmentDate, LocalTime appointmentStart, long employeeId);

}
