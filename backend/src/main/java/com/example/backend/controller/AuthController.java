package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.dto.SignInRequest;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.backend.dto.UserRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.service.UserService;
import com.example.backend.service.AuthService;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles user authentication for sign-in.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    /**
     * POST /api/users - Create a new user account.
     *
     * @param request Validated UserRequest payload.
     * @return UserResponse with created user details.
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = authService.createUser(request);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Handles sign-in with email and password.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest request) {

        String token = authService.authenticate(request.email, request.password);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);

        return ResponseEntity.ok(response);
    }
}
