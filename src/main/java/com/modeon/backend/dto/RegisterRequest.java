package com.modeon.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    private String username;

    @NotBlank(message = "email is required")
    @Email(message = "Email should be valid")
    private String Email;

    private String address;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "password must be at least 6")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;
}