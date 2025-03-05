package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.dto.UserUpdateRequest;
import com.example.backend.repository.UserRepository;
import com.example.backend.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Date;

/**
 * Service to handle business logic for user operations.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Retrieves paginated list of users.
     *
     * @param page  Page number (starting from 0)
     * @param limit Number of users per page
     * @return List of users
     */
    public List<User> getUsers(int page, int limit) {
        return userRepository.findAll()
                .stream()
                .skip((long) page * limit)
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> updateUser(String id, @Valid UserUpdateRequest updateRequest) {
        return userRepository.findById(id).map(user -> {
            if (updateRequest.getName() != null) {
                user.setName(updateRequest.getName());
            }
            if (updateRequest.getEmail() != null) {
                user.setEmail(updateRequest.getEmail());
            }
            user.setUpdated(new Date());
            return userRepository.save(user);
        });
    }

    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepository.delete(user);
    }
}
