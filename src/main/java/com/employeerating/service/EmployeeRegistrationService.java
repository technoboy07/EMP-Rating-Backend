package com.employeerating.service;

import com.employeerating.dto.EmployeeRegistrationReqDto;
import com.employeerating.entity.Employee;

public interface EmployeeRegistrationService { 
	Employee user(EmployeeRegistrationReqDto dto); 
	}