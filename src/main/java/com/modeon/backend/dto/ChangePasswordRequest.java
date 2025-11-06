package com.modeon.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "password must be at least 6")
    private String password;

    @NotBlank(message = "password2 is required")
    @Size(min = 6, message = "password must be at least 6")
    private String password2;

    @NotBlank(message = "authCode is required")
    private String authCode;
}