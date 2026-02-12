package com.employeerating.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employeerating.dto.EmployeeRegiResponse;
import com.employeerating.dto.EmployeeRegistrationReqDto;
import com.employeerating.entity.Employee;
import com.employeerating.service.EmployeeRegistrationService;
import com.employeerating.serviceImpl.EmployeeRegistrationServiceImpl;

@RestController
@RequestMapping("/api")
public class EmployeeRegistrationController {
	
	@Autowired
	private EmployeeRegistrationService service;
	
	@PostMapping("/register")
	public ResponseEntity<EmployeeRegiResponse> register(@Valid @RequestBody EmployeeRegistrationReqDto dto) {
		Employee saved = service.user(dto);
		EmployeeRegiResponse resp = new EmployeeRegiResponse(
                saved.getId(),
                saved.getEmployeeId(),
                saved.getEmployeeName(),
                saved.getEmployeeRole(),
                "Registration successful"
        );
		return ResponseEntity.ok(resp); 
		}
	@GetMapping("/employees/export")
	public ResponseEntity<String> exportEmployees() {
	    String filePath = "exports/employees.xlsx";  //Save inside exports folder
	    ((EmployeeRegistrationServiceImpl) service).exportEmployeesToExcel(filePath);
	    return ResponseEntity.ok("Employees exported successfully to: " + filePath);
	}
	}

