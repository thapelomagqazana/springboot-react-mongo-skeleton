package com.example.backend.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for sign-in requests.
 */
public class SignInRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    public String email;

    @NotBlank(message = "Password is required")
    public String password;
}