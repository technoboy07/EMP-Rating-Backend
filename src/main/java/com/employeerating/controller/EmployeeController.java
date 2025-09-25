package com.employeerating.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.employeerating.dto.EmployeeResponse;
import com.employeerating.entity.Employee;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.repository.RatingRepo;
import com.employeerating.service.RatingService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.employeerating.dto.FormData;
import com.employeerating.model.FileAttachmentModel;
import com.employeerating.service.EmailSchedulerService;
import com.employeerating.service.EmailSenderService;
import com.employeerating.service.EmployeeService;
import com.employeerating.util.ExcelGenerator;

import javax.transaction.Transactional;

@RestController
@RequestMapping("/api")
public class EmployeeController {
	@Autowired
	EmployeeService employeeService;
	@Autowired
	EmailSenderService emailSenderService;
	@Autowired
	EmailSchedulerService scheduleService;
    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    RatingRepo ratingRepo;
    private RatingService ratingService;
    @Autowired
    private EmailSchedulerService emailSchedulerService;


    //	@PostMapping("/save")
//	public ResponseEntity<?> saveEmployee(@RequestBody List<EmployeeDto> employeeDto) {
//		return employeeService.save(employeeDto);
//	}
	@PostMapping("/save")
	public ResponseEntity<?> saveEmployees(@RequestBody FormData formData ) {
		System.out.println("Hey i am prinbting");
		return employeeService.save(formData);
	}
	@GetMapping("/send")
	public String sendEmail(@RequestParam(name = "toEmail") String toEmail,
			@RequestParam(name = "subject") String subject, @RequestParam(name = "body") String body) {
		FileAttachmentModel model = new FileAttachmentModel(toEmail, body, subject);
		emailSenderService.sendEmail(model);
		return "Mail send successfully";
	}

    @GetMapping("/{employeeId}")
    public EmployeeResponse getEmployeeById(@PathVariable("employeeId") String employeeId) {
        return employeeService.fetchEmployeeById(employeeId);
    }

	@PostMapping("/sendFile")
	public String sendEmailWithImageAndFile(@RequestBody FileAttachmentModel model) {
		emailSenderService.sendEmailWithAttachmentToTl(model);
		return "email sent successfully to " + model.getToEmail();
	}

	@GetMapping("/fetchAll")
	public ResponseEntity<?> fetchAll() {
		return employeeService.fetchAll();
	}

	@GetMapping("/fetchAll/{teamLeadEmail}")
	public ResponseEntity<?> fetchAllByTeamLeadEmail(@PathVariable String teamLeadEmail) {
		return employeeService.fetchAllByTeamLeadEmail(teamLeadEmail);
	}

	@GetMapping("/get")
	public ResponseEntity<?> getEmployeeWithSpecificData(
			@RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return employeeService.getEmployee(date);
	}

	@GetMapping("/getByCriteria")
	public ResponseEntity<?> getEmployeeWithCriteriaManagerEmail(
			@RequestParam(name = "managerEmail") String managerEmail) {
		return employeeService.getByCriteria(managerEmail);
	}

	@GetMapping("/getEmail")
	public String getMethodName(@RequestParam String param  ) {
		return new String("Get Email");
	}

	@GetMapping("/getText")
	public String getMethodText(@RequestParam(name = "param") String param) {
		return "hii";
	}

	@GetMapping("/health")
	public String healthCheck() {
		return "Employee Rating System is running!";
	}

	@DeleteMapping("/delete/{empid}")
	public ResponseEntity<?> delete(@PathVariable(name = "empid") String empid) {
		return employeeService.deleteDetails(empid);
	}

	@PostMapping("/sendpdf")
	public void sendingTestMail() {
		scheduleService.sendEmailToHr();
	}

	@PostMapping("/")
	public String sendingTemplate() {
		return "emailTemplate";
	}

	@GetMapping("/employee")
	public ResponseEntity<byte[]> excelSendToProjectManager(@RequestParam(name="manager")String manager) throws InvalidFormatException{
		byte[] excelData = employeeService.generateEmployeesExcel(manager);

	    HttpHeaders headers = new HttpHeaders();

	    headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

	    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_rating.xlsx");

	    return ResponseEntity.ok()
	            .headers(headers)
	            .body(excelData);
	}

	@GetMapping("/employees")
	public ResponseEntity<byte[]> excelSendToProjectManagerOfficer(@RequestParam(name="managerOfficer")String managerOfficer) throws InvalidFormatException{
		byte[] excelData = employeeService.generateEmployeesExcelForManagerOfficer(managerOfficer);

	    HttpHeaders headers = new HttpHeaders();

	    headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_rating_pmo.xlsx");

	    return ResponseEntity.ok()
	            .headers(headers)
	            .body(excelData);
	}
	@GetMapping("/employeesHr")
	public ResponseEntity<byte[]> excelSendToHr() throws InvalidFormatException{
		byte[] excelData = employeeService.generateEmployeesExcelHr();

	    HttpHeaders headers = new HttpHeaders();

	    headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_rating_hr.xlsx");

	    return ResponseEntity.ok()
	            .headers(headers)
	            .body(excelData);
	}

	@GetMapping("/employee/{id}")
	public ResponseEntity<byte[]> downloadExcel(@PathVariable String id) {
	    byte[] excelData = employeeService.generateEmployeeExcel(id);

	    HttpHeaders headers = new HttpHeaders();

	    headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

	    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_rating.xlsx");

	    return ResponseEntity.ok()
	            .headers(headers)
	            .body(excelData);
	}


	@GetMapping("/simple-excel")
	public ResponseEntity<byte[]> downloadSimpleExcel() throws IOException {
	    byte[] data = ExcelGenerator.generateSimpleExcel();

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	    headers.setContentDispositionFormData("attachment", "simple_employee_list.xlsx");
	    headers.setContentLength(data.length);

	    return ResponseEntity.ok().headers(headers).body(data);
	}

    @GetMapping("/allemployeeids")
    public ResponseEntity<List<String>> getAllEmployeeIds() {
        List<String> ids = employeeService.getAllEmployeeIds();
        return ResponseEntity.ok(ids);
	}


    @GetMapping("/download-excel-mail")
    @Transactional
    public ResponseEntity<byte[]> downloadExcelMail(@RequestParam String pmEmail) throws IOException {
        List<Employee> employees = employeeRepo.findByProjectManagerEmail(pmEmail);



        System.out.println("Found employees: " + employees.size());
        employees.forEach(emp -> {
            System.out.println("Employee: " + emp.getEmployeeId() + " - " + emp.getEmployeeName());
            if (emp.getRatings() != null) {
                System.out.println("Ratings count: " + emp.getRatings().size());
            } else {
                System.out.println("No ratings for employee");
            }
        });

        employees.forEach(emp -> {
            if (emp.getRatings() != null) emp.getRatings().size(); // force load
        });

        byte[] excelBytes = ExcelGenerator.generateExcelForEmployeesPM(employees);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("Employee_Ratings.xlsx").build()
        );
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    @GetMapping("/download-excel-mail-tl")
    @Transactional
    public ResponseEntity<byte[]> downloadExcelMailTl(@RequestParam String teamLeadEmail) throws IOException {
       // List<Employee> employees = employeeRepo.findByTeamLeadEmail(teamLeadEmail);

        List<Employee> employees = employeeRepo.findAll();

        employees.forEach(emp -> {
            if (emp.getRatings() != null) emp.getRatings().size();         // forces loading ratings
            if (emp.getEmployeeTasks() != null) emp.getEmployeeTasks().size(); // forces loading tasks
        });

        if (employees == null || employees.isEmpty()) {
            // return empty Excel instead of crash
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        byte[] excelBytes = ExcelGenerator.generateExcelForEmployees(employees);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Employee.xlsx");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/delete")
    public ResponseEntity<List<String>> deletePreviousRatings(){
       return new ResponseEntity<>(emailSchedulerService.deletePreviousRatings(),HttpStatus.OK);
    }
}