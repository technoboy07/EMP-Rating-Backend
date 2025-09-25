package com.employeerating.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRegistrationReqDto {
	
	private Long id;
	
    @NotBlank(message = "Employee ID is required")
	private String employeeId;
    
    @NotBlank(message = "Employee name is required")
	private String employeeName; 
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
	private String password; 
    
    @NotBlank(message = "Confirm password is required")
	private String confirmPassword; // only for validation, not stored
    
    @NotBlank(message = "Employee role is required")
	private String employeeRole;
    
    // Optional fields that can be provided during registration
    @Email(message = "Invalid email format")
    private String employeeEmail;
    
    private String projectManagerName;
    
    @Email(message = "Invalid email format")
    private String projectManagerEmail;
}
