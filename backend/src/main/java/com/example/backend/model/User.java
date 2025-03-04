package com.example.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

/**
 * MongoDB entity for storing user data.
 */
@Document(collection = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String id;

    private String name;

    private String email;

    private String password;

    private Date created = new Date();

    private Date updated = new Date();
}
