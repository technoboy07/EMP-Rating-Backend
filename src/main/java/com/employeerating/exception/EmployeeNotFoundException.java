package com.employeerating.exception;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String employeeId) {
        super("Employee with ID " + employeeId + " not found");
    }
}