package com.ddimitko.beautyshopproject.Dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class EmployeeResponseDto {

    private Long userId;
    private Long shopId;
    private String fullName;
    private String email;
    private List<ServiceResponseDto> serviceList;

    //private List<AppointmentResponseDto> madeAppointments;
    //private List<AppointmentResponseDto> assignedAppointments;

}
