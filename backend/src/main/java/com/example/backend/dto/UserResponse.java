package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;

/**
 * DTO for returning user details after successful creation.
 */
@Data
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private Date created;
    private Date updated;
}
