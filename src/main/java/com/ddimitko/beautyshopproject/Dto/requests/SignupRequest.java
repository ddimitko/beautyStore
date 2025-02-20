package com.ddimitko.beautyshopproject.Dto.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String password;

}
