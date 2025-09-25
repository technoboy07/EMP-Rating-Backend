package com.employeerating.serviceImpl;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.employeerating.dto.EmployeeRegistrationReqDto;
import com.employeerating.entity.Employee;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.service.EmployeeRegistrationService;

@Service
public class EmployeeRegistrationServiceImpl implements EmployeeRegistrationService {

    @Autowired
    private EmployeeRepo repo;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
//    @Autowired
//    private PasswordEncoder passwordEncoder;

	@Override
	public Employee user(EmployeeRegistrationReqDto dto) {
		if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (repo.existsByEmployeeId(dto.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID already exists");
        }

        Employee employee = new Employee();
        employee.setEmployeeId(dto.getEmployeeId());
        employee.setEmployeeName(dto.getEmployeeName());

        String encodePassword = passwordEncoder.encode(dto.getPassword());
        employee.setPassword(encodePassword);
        employee.setEmployeeRole(dto.getEmployeeRole());
        
        // Set values from DTO if provided, otherwise use defaults for required fields
        employee.setEmployeeEmail(dto.getEmployeeEmail() != null ? dto.getEmployeeEmail() : "temp@company.com");
        employee.setProjectManagerName(dto.getProjectManagerName() != null ? dto.getProjectManagerName() : "TBD");
        employee.setProjectManagerEmail(dto.getProjectManagerEmail() != null ? dto.getProjectManagerEmail() : "pm@company.com");
        
        // Set nullable Boolean fields to null (not false)
        employee.setPmSubmitted(null);
        employee.setIsTLSubmitted(null);
        employee.setIsHrSend(null);
        employee.setIsPmoSubmitted(null);
        employee.setNoticePeriod(null);
        employee.setProbationaPeriod(null);


        Employee saved =  repo.save(employee);

        //Export (optional - called automatically after each registration)
        try {
            ensureExportsFolder();
            exportEmployeesToExcel("exports/employees.xlsx");
        } catch (Exception ignored) {
            // do not fail registration if export fails
        }
        return saved;
    }

    // Export Employees to Excel
    public void exportEmployeesToExcel(String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employees");

            //Create Header Row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Sl_No");
            header.createCell(1).setCellValue("employeeId");
            header.createCell(2).setCellValue("employeeName");
            header.createCell(3).setCellValue("employeeRole");

            
            List<Employee> employees = repo.findAll();
            int rowIdx = 1, serial = 1;
            for (Employee emp : employees) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(serial++);
                row.createCell(1).setCellValue(emp.getEmployeeId());
                row.createCell(2).setCellValue(emp.getEmployeeName());
                row.createCell(3).setCellValue(emp.getEmployeeRole());
            }
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to export Excel: " + e.getMessage(), e);
        }
    }
    
    private void ensureExportsFolder() throws Exception {
        Path p = Path.of("exports");
        if (!Files.exists(p)) Files.createDirectories(p);
    }
}
