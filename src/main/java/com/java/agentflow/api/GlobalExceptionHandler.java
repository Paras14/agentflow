package com.java.agentflow.api;

import com.java.agentflow.workflow.engine.InterpolationException;
import com.java.agentflow.workflow.parser.WorkflowParseException;
import com.java.agentflow.workflow.service.WorkflowAlreadyExistsException;
import com.java.agentflow.workflow.service.WorkflowNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Global exception handler for the API.
 * Provides consistent error responses and prevents leaking internal details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WorkflowNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(WorkflowNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(WorkflowAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(WorkflowAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse("CONFLICT", e.getMessage()));
    }

    @ExceptionHandler(WorkflowParseException.class)
    public ResponseEntity<Map<String, Object>> handleParseError(WorkflowParseException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse("PARSE_ERROR", e.getMessage()));
    }

    @ExceptionHandler(InterpolationException.class)
    public ResponseEntity<Map<String, Object>> handleInterpolationError(InterpolationException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse("INTERPOLATION_ERROR", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse("BAD_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception e) {
        // Log the full error for debugging
        log.error("Unexpected error", e);

        // Return generic message to avoid leaking internal details
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    private Map<String, Object> errorResponse(String code, String message) {
        return Map.of(
                "error", code,
                "message", message,
                "timestamp", Instant.now().toString());
    }
}
