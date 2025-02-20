package com.ddimitko.beautyshopproject.Dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;
    private String role;

    //private List<AppointmentResponseDto> madeAppointments;

}
