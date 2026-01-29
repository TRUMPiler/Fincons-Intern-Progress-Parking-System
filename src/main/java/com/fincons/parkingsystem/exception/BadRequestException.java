package com.fincons.parkingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for invalid client requests. Maps to HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    /**
     * @param message The error message.
     */
    public BadRequestException(String message) {
        super(message);
    }
}
