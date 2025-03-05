package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.dto.UserUpdateRequest;
import com.example.backend.exception.UserNotFoundException;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller to handle user-related operations such as:
 * - Listing users
 * - Retrieving a user by ID
 * - Updating a user
 * - Deleting a user
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    /**
     * Retrieves a paginated list of users.
     *
     * @param page  page number (starting from 0)
     * @param limit number of users per page
     * @return List of users or 500 on failure
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<User>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int limit
    ) {
        logger.info("Fetching users - page: {}, limit: {}", page, limit);
        try {
            List<User> users = userService.getUsers(page, limit);
            logger.info("Fetched {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id User ID
     * @return User object if found or appropriate error response
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        logger.info("Fetching user with ID: {}", id);

        if (!id.matches("^[a-fA-F0-9]{24}$")) {
            logger.warn("Invalid ID format: {}", id);
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Invalid ID format")
            );
        }

        try {
            Optional<User> user = userService.findById(id);

            if (user.isEmpty()) {
                logger.warn("User not found with ID: {}", id);
                return ResponseEntity.status(404).body(
                    new ErrorResponse("User not found")
                );
            }

            logger.info("User found with ID: {}", id);
            return ResponseEntity.ok(user.get());
        } catch (Exception e) {
            logger.error("Error fetching user with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(
                new ErrorResponse("Internal server error")
            );
        }
    }

    /**
     * Updates a user by ID.
     *
     * @param id             User ID
     * @param updateRequest  User update payload
     * @return Updated user object or error response
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserUpdateRequest updateRequest
    ) {
        logger.info("Updating user with ID: {}", id);

        if (!id.matches("^[a-fA-F0-9]{24}$")) {
            logger.warn("Invalid ID format for update: {}", id);
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Invalid ID format")
            );
        }

        if (updateRequest.getName() == null && updateRequest.getEmail() == null) {
            logger.warn("Update request for ID {} contains no data", id);
            return ResponseEntity.badRequest().body(
                new ErrorResponse("No data provided")
            );
        }

        Optional<User> updatedUser = userService.updateUser(id, updateRequest);

        if (updatedUser.isPresent()) {
            logger.info("User with ID {} updated successfully", id);
            return ResponseEntity.ok(updatedUser.get());
        } else {
            logger.warn("User with ID {} not found for update", id);
            return ResponseEntity.status(404).body(
                new ErrorResponse("User not found")
            );
        }
    }

    /**
     * Deletes a user by ID.
     *
     * @param id User ID
     * @return 204 No Content if successful or appropriate error response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteUser(
            @PathVariable("id") String id) {
        logger.info("Deleting user with ID: {}", id);

        if (!id.matches("^[a-fA-F0-9]{24}$")) {
            logger.warn("Invalid ID format for deletion: {}", id);
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Invalid ID format")
            );
        }

        try {
            userService.deleteUser(id);
            logger.info("User with ID {} deleted successfully", id);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException ex) {
            logger.error("Error deleting user with ID {}: {}", id, ex.getMessage());
            return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
        } catch (Exception ex) {
            logger.error("Unexpected error deleting user with ID {}: {}", id, ex.getMessage());
            return ResponseEntity.status(500).body(new ErrorResponse("Internal server error"));
        }
    }

    /**
     * Error response wrapper.
     *
     * @param message Error message
     */
    record ErrorResponse(String message) {}
}
