package com.workhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handles @Valid failures -> 400 BAD REQUEST
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed");

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        response.put("details", fieldErrors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 2. Handles Missing Data (e.g., Project not found) -> 404 NOT FOUND
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundExceptions(ResourceNotFoundException ex) {
        return new ResponseEntity<>(
                buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    // 3. Handles Tenant Leaks (e.g., Accessing Tenant B's data) -> 403 FORBIDDEN (Or change to 400 if you prefer)
    @ExceptionHandler(TenantAccessException.class)
    public ResponseEntity<Map<String, Object>> handleTenantAccessExceptions(TenantAccessException ex) {
        // Note: I set this to 403 Forbidden as it is standard for access denied,
        // but you can change it to HttpStatus.BAD_REQUEST if you specifically want a 400 status.
        return new ResponseEntity<>(
                buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }

    // 4. Handles Business Logic Errors (e.g., "FAIL" title) -> 400 BAD REQUEST
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessLogicExceptions(IllegalArgumentException ex) {
        return new ResponseEntity<>(
                buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    // 5. Catch-all for unexpected crashes -> 500 INTERNAL SERVER ERROR
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericExceptions(Exception ex) {
        return new ResponseEntity<>(
                buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // Helper method that formats the JSON EXACTLY how you requested
    private Map<String, Object> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("error", message);
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());

        return response;
    }
}