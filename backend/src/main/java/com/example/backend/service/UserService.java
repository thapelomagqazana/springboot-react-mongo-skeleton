package com.example.backend.service;

import com.example.backend.dto.UserRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.exception.UserAlreadyExistsException;
import com.example.backend.exception.PayloadTooLargeException;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user business logic and validations before saving.
 */
@Service
public class UserService {

    private static final int MAX_PAYLOAD_SIZE = 10_000_000; // 10MB

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Creates a new user with validation, duplicate checking, and password hashing.
     *
     * @param request UserRequest object containing the user data.
     * @return UserResponse with the created user's public details.
     */
    public UserResponse createUser(UserRequest request) {

        if (request.toString().length() > MAX_PAYLOAD_SIZE) {
            throw new PayloadTooLargeException("Payload too large");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        // Map DTO to Entity
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save to DB
        User savedUser = userRepository.save(user);

        // Map Entity to Response DTO
        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreated(),
                savedUser.getUpdated()
        );
    }
}
