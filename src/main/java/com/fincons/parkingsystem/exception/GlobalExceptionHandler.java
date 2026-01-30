package com.fincons.parkingsystem.exception;

import com.fincons.parkingsystem.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Catches and handles exceptions for all controllers.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from @Valid.
     *
     * @param ex The validation exception.
     * @return A response with a map of validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        log.error(STR."""
Request ID\{request.getSessionId()} is facing  \{ex.getMessage()}
\{ex}""");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        Response<Map<String, String>> response = new Response<>(LocalDateTime.now(), errors, "Validation Failed", false, HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles resource not found errors.
     *
     * @param ex The resource not found exception.
     * @return A response with a 404 status and error message.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Response<String>> handleResourceNotFoundException(ResourceNotFoundException ex,WebRequest request)
    {
        log.error(STR."""
Request ID\{request.getSessionId()} is facing  \{ex.getMessage()}
\{ex}""");
        Response<String> response = new Response<>(LocalDateTime.now(), null, ex.getMessage(), false, HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles data conflict errors.
     *
     * @param ex The conflict exception.
     * @return A response with a 409 status and error message.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Response<String>> handleConflictException(ConflictException ex,WebRequest request)
    {
        log.error(STR."""
Request ID\{request.getSessionId()} is facing  \{ex.getMessage()}
\{ex}""");
        Response<String> response = new Response<>(LocalDateTime.now(), null, ex.getMessage(), false, HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handles bad request errors.
     *
     * @param ex The bad request exception.
     * @return A response with a 400 status and error message.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Response<String>> handleBadRequestException(BadRequestException ex,WebRequest request) {
        log.error(STR."""
Request ID\{request.getSessionId()} is facing  \{ex.getMessage()}
\{ex}""");
        Response<String> response = new Response<>(LocalDateTime.now(), null, ex.getMessage(), false, HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other uncaught exceptions.
     *
     * @param ex The exception.
     * @return A response with a 500 status and generic error message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<String>> handleAllExceptions(Exception ex,WebRequest request) {
        log.error(STR."""
Request ID\{request.getSessionId()} is facing  \{ex.getMessage()}
\{ex}""");
        ex.printStackTrace();
        Response<String> response = new Response<>(LocalDateTime.now(), null, "An unexpected error occurred: " + ex.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
