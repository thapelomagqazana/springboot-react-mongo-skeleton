package com.example.backend.exception;

/**
 * Thrown when a user tries to register with an email that already exists.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
