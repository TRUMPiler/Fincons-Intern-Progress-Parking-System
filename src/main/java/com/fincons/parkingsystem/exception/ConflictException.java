package com.fincons.parkingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to indicate a conflict with the current state of the resource.
 * This typically maps to an HTTP 409 Conflict status code.
 * Examples include:
 * - Attempting to create a resource that already exists (e.g., parking lot with same name).
 * - Attempting an operation that violates a business rule (e.g., vehicle already has an active session).
 * - No available parking slots.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
    /**
     * Constructs a new ConflictException with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     */
    public ConflictException(String message) {
        super(message);
    }
}
