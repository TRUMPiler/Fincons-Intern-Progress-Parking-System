package com.fincons.parkingsystem.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;

/**
 * A standardized class for representing error responses in the API.
 * This class is used to create a consistent and informative error message format
 * when an exception occurs. It includes details such as the timestamp, HTTP status,
 * error type, a descriptive message, and the path where the error occurred.
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    /**
     * The timestamp when the error occurred.
     */
    private Instant timestamp;
    /**
     * The HTTP status code associated with the error (e.g., 400, 404, 500).
     */
    private int status;
    /**
     * A short, machine-readable string representing the error type (e.g., "Bad Request").
     */
    private String error;
    /**
     * A human-readable message providing more details about the error.
     */
    private String message;
    /**
     * The request path that resulted in the error.
     */
    private String path;
}
