package com.ddimitko.beautyshopproject.Dto.requests;

import com.ddimitko.beautyshopproject.Dto.calendar.TimeSlotDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentRequestDto {

    private Long appointmentId;
    private Long customerId;
    private Long employeeId;
    private Integer serviceId;

    private String fullName;
    private String email;
    private String phone;

    private TimeSlotDto timeSlotDto;

}
