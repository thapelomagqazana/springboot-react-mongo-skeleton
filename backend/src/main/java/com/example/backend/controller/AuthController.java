package com.example.backend.controller;

import com.example.backend.dto.SignInRequest;
import com.example.backend.dto.UserRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.security.JwtUtil;
import com.example.backend.security.TokenBlacklistService;
import com.example.backend.service.AuthService;
import com.example.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles authentication endpoints including:
 * - User sign-up (/signup)
 * - User sign-in (/signin)
 * - User sign-out (/signout)
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and logout")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    /**
     * Registers a new user.
     *
     * @param request the user registration request payload
     * @return the created user details
     */
    @Operation(
            summary = "User Signup",
            description = "Register a new user with name, email, and password."
    )
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        logger.info("Attempting to create user with email: {}", request.getEmail());

        UserResponse response = authService.createUser(request);

        logger.info("User created successfully with ID: {}", response.getId());
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request the sign-in request payload
     * @return a JWT token if authentication is successful
     */
    @Operation(
            summary = "User Sign-in",
            description = "Authenticate a user with email and password to receive a JWT token."
    )
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest request) {
        logger.info("Attempting sign-in for email: {}", request.getEmail());

        String token = authService.authenticate(request.getEmail(), request.getPassword());

        logger.info("Sign-in successful for email: {}", request.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    /**
     * Invalidates a user's JWT token (sign-out).
     *
     * @param request the HTTP request containing the Authorization header
     * @return response indicating success or failure of sign-out
     */
    @Operation(
            summary = "User Sign-out",
            description = "Sign out the current user and invalidate their JWT token."
    )
    @PostMapping("/signout")
    public ResponseEntity<?> signOut(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Sign-out failed: Missing or invalid Authorization header.");
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            logger.warn("Sign-out failed: Invalid or expired token.");
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            logger.warn("Sign-out failed: Token already blacklisted.");
            return ResponseEntity.status(401).body(Map.of("message", "Token invalid"));
        }

        tokenBlacklistService.blacklistToken(token);
        logger.info("Token successfully invalidated and user signed out.");

        return ResponseEntity.ok(Map.of("message", "Signed out successfully."));
    }
}
