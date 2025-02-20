package com.ddimitko.beautyshopproject.Dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AppointmentResponseDto {

    private long appointmentId;
    private String shopName;
    private Long customerId;
    private String customerName;

    private long employeeId;
    private String employeeName;

    private int serviceId;
    private String serviceName;

    private String status;

    private LocalDateTime appointmentDate;

}
