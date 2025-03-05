package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.dto.UserUpdateRequest;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

/**
 * Controller to handle user-related operations, including listing users.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users
     * Returns a paginated list of users.
     *
     * @param page  page number (starting from 0)
     * @param limit number of users per page
     * @return List of users
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<User>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int limit
    ) {
        try {
            List<User> users = userService.getUsers(page, limit);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).build(); // For DB failures (TC_LU_008)
        }
    }

    /**
     * GET /api/users/{id}
     * Retrieves a user by their ID.
     *
     * @param id User ID
     * @return User object if found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        if (!id.matches("^[a-fA-F0-9]{24}$")) { // Simple ObjectId format check
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Invalid ID format")
            );
        }

        try {
            Optional<User> user = userService.findById(id);

            if (user.isEmpty()) {
                return ResponseEntity.status(404).body(
                    new ErrorResponse("User not found")
                );
            }

            return ResponseEntity.ok(user.get());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ErrorResponse("Internal server error")
            );
        }
    }

    record ErrorResponse(String message) {}

    /**
     * PUT /api/users/{id}
     * Updates a user by ID.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserUpdateRequest updateRequest
    ) {
        if (!id.matches("^[a-fA-F0-9]{24}$")) { // Simple ObjectId format check
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Invalid ID format")
            );
        }

        if (updateRequest.getName() == null && updateRequest.getEmail() == null) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("No data provided")
            );
        }

        Optional<User> updatedUser = userService.updateUser(id, updateRequest);

        return updatedUser
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(
                    new ErrorResponse("User not found")
                ));
    }
}
