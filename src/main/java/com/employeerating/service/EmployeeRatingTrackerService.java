package com.employeerating.service;

import org.springframework.http.ResponseEntity;

import com.employeerating.dto.EmployeeRatingTrackerDto;

public interface EmployeeRatingTrackerService {

	ResponseEntity<?> save(EmployeeRatingTrackerDto dto);

	ResponseEntity<?> tlSubmit(String employeeId);

	ResponseEntity<?> pmSubmit(String employeeId);

	ResponseEntity<?> pmoSubmit(String employeeId);

}
