package com.example.backend.exception;

/**
 * Thrown when payload size exceeds the acceptable limit.
 */
public class PayloadTooLargeException extends RuntimeException {
    public PayloadTooLargeException(String message) {
        super(message);
    }
}
