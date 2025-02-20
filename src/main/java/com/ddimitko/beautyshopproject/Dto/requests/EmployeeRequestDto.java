package com.ddimitko.beautyshopproject.Dto.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmployeeRequestDto {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Long shopId;

}
