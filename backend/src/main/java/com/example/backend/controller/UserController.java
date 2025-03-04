package com.example.backend.controller;

import com.example.backend.dto.UserRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * REST controller for User creation.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * POST /api/users - Create a new user account.
     *
     * @param request Validated UserRequest payload.
     * @return UserResponse with created user details.
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(201).body(response);
    }
}
