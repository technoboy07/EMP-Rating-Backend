package com.employeerating.service;

import java.time.LocalDate;
import java.util.List;

import com.employeerating.dto.EmployeeResponse;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.http.ResponseEntity;

import com.employeerating.dto.EmployeeDto;
import com.employeerating.dto.FormData;

public interface EmployeeService {

	//ResponseEntity<?> save(List<EmployeeDto> dto);

	ResponseEntity<?> fetchAll();

	ResponseEntity<?> fetchAllByTeamLeadEmail(String teamLeadEmail);

	ResponseEntity<?> getEmployee(LocalDate date);

	ResponseEntity<?> getByCriteria(String managerEmail);
	
	ResponseEntity<?> deleteDetails(String id);

//	byte[] generateEmployeeExcel(String employeeId);

	byte[] generateEmployeesExcel(String manager) throws InvalidFormatException;
	
	ResponseEntity<?> save(List<EmployeeDto> dto);
	ResponseEntity<?> save(FormData formData);

	byte[] generateEmployeesExcelForManagerOfficer(String managerOfficer);

	byte[] generateEmployeesExcelHr();

    List<String> getAllEmployeeIds();
    
    ResponseEntity<?> updateEmployee(EmployeeDto dto);
    
    ResponseEntity<?> saveSingleEmployee(EmployeeDto dto);

    EmployeeResponse fetchEmployeeById(String employeeId);


}
