package com.ddimitko.beautyshopproject.mappers;

import com.ddimitko.beautyshopproject.Dto.responses.AppointmentResponseDto;
import com.ddimitko.beautyshopproject.entities.Appointment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AppointmentMapper {

    public AppointmentResponseDto mapToResponseDto(Appointment appointment) {

        Long customerId = null;

        if(appointment.getCustomer() != null) {
            customerId = appointment.getCustomer().getId();
        }

        return new AppointmentResponseDto(
                appointment.getId(), appointment.getEmployee().getShop().getName(), customerId,
                appointment.getFullName(), appointment.getEmployee().getUser().getId(),
                appointment.getEmployee().getUser().getFirstName(),
                appointment.getService().getId(), appointment.getService().getName(), appointment.getStatus().name(),
                LocalDateTime.of(appointment.getAppointmentDate(), appointment.getAppointmentStart())
        );
    }

}
