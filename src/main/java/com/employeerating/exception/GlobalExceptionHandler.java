package com.employeerating.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        Map<String, String> resp = new HashMap<>();
        resp.put("message", ex.getMessage());
        resp.put("status", "ERROR");
        return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        Map<String, String> resp = new HashMap<>();
        resp.put("message", ex.getMessage());
        resp.put("status", "ERROR");
        return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> resp = new HashMap<>();
        resp.put("message", ex.getMessage());
        resp.put("status", "ERROR");
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "Database constraint violation: " + ex.getMessage());
        resp.put("status", "ERROR");
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<Map<String, String>> handleJpaSystemException(JpaSystemException ex) {
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "Database operation failed: " + ex.getMessage());
        resp.put("status", "ERROR");
        return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Optional: catch-all for other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleOtherExceptions(Exception ex) {
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "Internal Server Error: " + ex.getMessage());
        resp.put("status", "ERROR");
        return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

