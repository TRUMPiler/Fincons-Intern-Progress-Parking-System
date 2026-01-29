package com.fincons.parkingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for when a requested resource is not found. Maps to HTTP 404 Not Found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    /**
     * @param message The error message.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
