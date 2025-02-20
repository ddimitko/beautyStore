package com.ddimitko.beautyshopproject.Dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {

    @Email(message = "Invalid email address.")
    private String email;

    @NotBlank
    @NotEmpty
    @NotNull
    private String password;

}
