package com.lunacycle.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        Map<String, String> validationErrors,
        LocalDateTime timestamp
) {
    // Constructor for standard errors
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, null, LocalDateTime.now());
    }

    // Constructor for validation errors
    public ErrorResponse(int status, String error, String message, Map<String, String> validationErrors) {
        this(status, error, message, validationErrors, LocalDateTime.now());
    }
}