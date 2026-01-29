package com.fincons.parkingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to indicate that a client's request is malformed or invalid.
 * This typically maps to an HTTP 400 Bad Request status code.
 * Examples include:
 * - Missing required request parameters.
 * - Invalid data formats.
 * - An exit time that is before an entry time.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    /**
     * Constructs a new BadRequestException with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     */
    public BadRequestException(String message) {
        super(message);
    }
}
