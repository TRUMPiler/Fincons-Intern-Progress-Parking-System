package com.fincons.parkingsystem.exception;

import com.fincons.parkingsystem.utils.Response;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * This class uses @RestControllerAdvice to centralize exception handling logic
 * across all controllers, ensuring consistent error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation exceptions triggered by @Valid annotations on controller method arguments.
     * This method extracts field-specific errors and returns them in a structured map.
     *
     * @param ex The MethodArgumentNotValidException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity containing a map of field names to error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation error for request {}:\n {}", request.getDescription(false), ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        Response<Map<String, String>> response = new Response<>(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), errors, "Validation Failed", false, HttpStatus.BAD_REQUEST.value());
        log.info(response.toString());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles exceptions thrown when a requested resource cannot be found.
     *
     * @param ex The ResourceNotFoundException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 404 Not Found status and error message.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Response<String>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found for request {}: {}", request.getDescription(false), ex.getMessage());
        Response<String> response = new Response<>(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), null, ex.getMessage(), false, HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles exceptions thrown due to a conflict with the current state of a resource,
     * such as attempting to create a resource that already exists.
     *
     * @param ex The ConflictException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 409 Conflict status and error message.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Response<String>> handleConflictException(ConflictException ex, WebRequest request) {
        log.error("Conflict error for request {}: {}", request.getDescription(false), ex.getMessage());
        Response<String> response = new Response<>(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), null, ex.getMessage(), false, HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handles exceptions thrown due to optimistic locking failures. This occurs when two
     * concurrent transactions attempt to update the same entity, and one of them fails.
     *
     * @param ex The OptimisticLockException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 409 Conflict status and a user-friendly error message.
     */
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<Response<String>> handleOptimisticLockException(OptimisticLockException ex, WebRequest request) {
        log.warn("Optimistic locking conflict for request {}: {}", request.getDescription(false), ex.getMessage());
        Response<String> response = new Response<>(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), null, "The data has been modified by another process. Please try your request again.", false, HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handles exceptions related to invalid or malformed client requests.
     *
     * @param ex The BadRequestException that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 400 Bad Request status and error message.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Response<String>> handleBadRequestException(BadRequestException ex, WebRequest request) {
        log.error("Bad request for request {}: {}", request.getDescription(false), ex.getMessage());
        Response<String> response = new Response<>(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), null, ex.getMessage(), false, HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * A generic handler for all other uncaught exceptions, serving as a fallback.
     * This prevents the application from exposing raw stack traces to the client.
     *
     * @param ex The Exception that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity with a 500 Internal Server Error status and a generic error message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<String>> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("An unexpected error occurred for request {}: {}", request.getDescription(false), ex.getMessage(), ex);
        Response<String> response = new Response<>(Instant.now().atZone(java.time.ZoneId.systemDefault()).toInstant(), null, "An unexpected error occurred. Please contact support.", false, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
