package com.example.backend.dto;

import lombok.Data;

import jakarta.validation.constraints.*;

/**
 * DTO for handling user creation requests.
 * Includes validation constraints for each field.
 */
@Data
public class UserRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    private String password;

    private String role;
}
