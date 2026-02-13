package com.employeerating.controller;

import com.employeerating.dto.EmployeeResponse;
import com.employeerating.dto.FormData;
import com.employeerating.entity.Employee;
import com.employeerating.model.FileAttachmentModel;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.service.EmailSchedulerService;
import com.employeerating.service.EmailSenderService;
import com.employeerating.service.EmployeeService;
import com.employeerating.util.ExcelGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
public class EmployeeController {

	@Autowired
	private EmployeeService employeeService;

	@Autowired
	private EmailSenderService emailSenderService;

	@Autowired
	private EmailSchedulerService emailSchedulerService;

	@Autowired
	private EmployeeRepo employeeRepo;

	// ================= CREATE / SAVE =================

	@PostMapping("/save")
	public ResponseEntity<?> saveEmployees(@RequestBody FormData formData) {
		log.debug("Processing employee save request");
		return employeeService.save(formData);
	}

	// ================= FETCH =================

	@GetMapping("/{employeeId}")
	public EmployeeResponse getEmployeeById(@PathVariable String employeeId) {
		log.debug("Fetching employee by id: {}", employeeId);
		return employeeService.fetchEmployeeById(employeeId);
	}

	@GetMapping("/fetchAll")
	public ResponseEntity<?> fetchAll() {
		return employeeService.fetchAll();
	}

	@GetMapping("/teamleads/names")
	public ResponseEntity<List<String>> getAllTeamLeadNames() {
		List<Employee> teamLeads = employeeRepo.findByEmployeeRoleContainingIgnoreCase("team lead");
		List<String> teamLeadNames = teamLeads.stream()
			.map(Employee::getEmployeeName)
			.filter(name -> name != null && !name.trim().isEmpty())
			.distinct()
			.collect(Collectors.toList());
		return ResponseEntity.ok(teamLeadNames);
	}

	@GetMapping("/fetchAll/{teamLeadEmail}")
	public ResponseEntity<?> fetchAllByTeamLeadEmail(@PathVariable String teamLeadEmail) {
		return employeeService.fetchAllByTeamLeadEmail(teamLeadEmail);
	}

	@GetMapping("/get")
	public ResponseEntity<?> getEmployeeByDate(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return employeeService.getEmployee(date);
	}

	@GetMapping("/getByCriteria")
	public ResponseEntity<?> getEmployeeByManagerEmail(@RequestParam String managerEmail) {
		return employeeService.getByCriteria(managerEmail);
	}

	@GetMapping("/allemployeeids")
	public ResponseEntity<List<String>> getAllEmployeeIds() {
		return ResponseEntity.ok(employeeService.getAllEmployeeIds());
	}

	// ================= EMAIL =================

	@GetMapping("/send")
	public String sendEmail(
			@RequestParam String toEmail,
			@RequestParam String subject,
			@RequestParam String body) {

		FileAttachmentModel model = new FileAttachmentModel(toEmail, body, subject);
		emailSenderService.sendEmail(model);
		return "Mail sent successfully";
	}

	@PostMapping("/sendFile")
	public String sendEmailWithAttachment(@RequestBody FileAttachmentModel model) {
		emailSenderService.sendEmailWithAttachmentToTl(model);
		return "Email sent successfully to " + model.getToEmail();
	}

	// ================= DELETE =================

	@DeleteMapping("/delete/{empid}")
	public ResponseEntity<?> delete(@PathVariable String empid) {
		return employeeService.deleteDetails(empid);
	}

	@GetMapping("/delete")
	public ResponseEntity<List<String>> deletePreviousRatings() {
		return ResponseEntity.ok(emailSchedulerService.deletePreviousRatings());
	}

	// ================= HEALTH =================

	@GetMapping("/health")
	public String healthCheck() {
		return "Employee Rating System is running!";
	}

	// ================= EXCEL DOWNLOADS =================

	@GetMapping("/employee")
	public ResponseEntity<byte[]> excelSendToProjectManager(@RequestParam String manager)
			throws InvalidFormatException {

		byte[] excelData = employeeService.generateEmployeesExcel(manager);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_rating.xlsx")
				.contentType(MediaType.parseMediaType(
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(excelData);
	}

	@GetMapping("/employees")
	public ResponseEntity<byte[]> excelSendToPMO(@RequestParam String managerOfficer)
			throws InvalidFormatException {

		byte[] excelData = employeeService.generateEmployeesExcelForManagerOfficer(managerOfficer);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_rating_pmo.xlsx")
				.contentType(MediaType.parseMediaType(
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(excelData);
	}

	@GetMapping("/employeesHr")
	public ResponseEntity<byte[]> excelSendToHr() throws InvalidFormatException {

		byte[] excelData = employeeService.generateEmployeesExcelHr();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_rating_hr.xlsx")
				.contentType(MediaType.parseMediaType(
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(excelData);
	}

	@GetMapping("/simple-excel")
	public ResponseEntity<byte[]> downloadSimpleExcel() throws IOException {

		byte[] data = ExcelGenerator.generateSimpleExcel();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=simple_employee_list.xlsx")
				.contentType(MediaType.parseMediaType(
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(data);
	}

	// ================= MAIL EXCEL =================

	@Transactional
	@GetMapping("/download-excel-mail")
	public ResponseEntity<byte[]> downloadExcelMail(@RequestParam String pmEmail) throws IOException {

		List<Employee> employees = employeeRepo.findByProjectManagerEmail(pmEmail);
		log.info("Found {} employees for PM {}", employees.size(), pmEmail);

		employees.forEach(emp -> {
			if (emp.getRatings() != null) emp.getRatings().size();
		});

		byte[] excelBytes = ExcelGenerator.generateExcelForEmployeesPM(employees);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename("Employee_Ratings.xlsx").build().toString())
				.contentType(MediaType.parseMediaType(
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(excelBytes);
	}

	@Transactional
	@GetMapping("/download-excel-mail-tl")
	public ResponseEntity<byte[]> downloadExcelMailTl(@RequestParam String teamLeadEmail)
			throws IOException {

		List<Employee> employees = employeeRepo.findAll();

		employees.forEach(emp -> {
			if (emp.getRatings() != null) emp.getRatings().size();
			if (emp.getEmployeeTasks() != null) emp.getEmployeeTasks().size();
		});

		if (employees.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		byte[] excelBytes = ExcelGenerator.generateExcelForEmployees(employees);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Employee.xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(excelBytes);
	}
}
