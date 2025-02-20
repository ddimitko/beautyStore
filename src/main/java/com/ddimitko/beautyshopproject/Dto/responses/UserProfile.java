package com.ddimitko.beautyshopproject.Dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserProfile {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

}
