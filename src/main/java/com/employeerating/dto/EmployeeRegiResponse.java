package com.employeerating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeeRegiResponse {
	
	 private Long id;
	 private String employeeId;
	 private String employeeName;
	 private String employeeRole;
	 private String message;

}
