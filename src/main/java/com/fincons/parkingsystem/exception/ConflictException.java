package com.fincons.parkingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for requests that conflict with the current state of a resource. Maps to HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
    /**
     * @param message The error message.
     */
    public ConflictException(String message) {
        super(message);
    }
}
