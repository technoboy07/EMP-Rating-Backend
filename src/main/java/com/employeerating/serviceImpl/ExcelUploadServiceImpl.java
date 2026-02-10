package com.employeerating.serviceImpl;

import com.employeerating.entity.Employee;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.service.ExcelUploadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class ExcelUploadServiceImpl implements ExcelUploadService {

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "Rumango@123";

    // ================= BASIC EMPLOYEE UPLOAD =================

    @Override
    public void uploadExcel(MultipartFile file) {

        validateFile(file);

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String empId = getCellValue(row.getCell(1));
                String name  = getCellValue(row.getCell(2));
                String role  = getCellValue(row.getCell(3));
                String email = getCellValue(row.getCell(4));

                if (isBlank(empId, name, role, email)) continue;

                if (employeeRepo.findByEmployeeId(empId.trim()).isPresent()) {
                    log.warn("Employee {} already exists, skipping basic upload", empId);
                    continue;
                }

                Employee employee = new Employee();
                employee.setEmployeeId(empId.trim());
                employee.setEmployeeName(name.trim());
                employee.setEmployeeRole(role.trim());
                employee.setEmployeeEmail(email.trim());
                employee.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));

                employee.setProjectManagerName("TBD");
                employee.setProjectManagerEmail("pm@company.com");

                employeeRepo.save(employee);
            }

        } catch (Exception e) {
            log.error("Failed to upload employee Excel", e);
            throw new RuntimeException("Failed to upload employee Excel", e);
        }
    }

    // ================= DETAILED EMPLOYEE UPLOAD (FIXED) =================

    @Override
    public void uploadEmployeeDetailsExcel(MultipartFile file) {

        validateFile(file);

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String empId = getCellValue(row.getCell(1));
                if (empId == null || empId.isBlank()) continue;

                Optional<Employee> optionalEmployee =
                        employeeRepo.findByEmployeeId(empId.trim());

                // ✅ FIX: Update if exists, else create
                Employee emp = optionalEmployee.orElseGet(Employee::new);
                emp.setEmployeeId(empId.trim());

                emp.setEmployeeName(getCellValue(row.getCell(2)));
                emp.setEmployeeRole(getCellValue(row.getCell(3)));
                emp.setEmployeeEmail(getCellValue(row.getCell(4)));
                emp.setEmploymentType(getCellValue(row.getCell(5)));

                emp.setJoiningDate(parseDate(row.getCell(6)));
                emp.setLeaveDate(parseDate(row.getCell(7)));

                emp.setNoticePeriod(parseBoolean(row.getCell(8)));
                emp.setProbationaPeriod(parseBoolean(row.getCell(9)));

                emp.setProjectManagerEmail(getCellValue(row.getCell(10)));
                emp.setProjectManagerName("TBD");
                emp.setProjectName(getCellValue(row.getCell(11)));
                emp.setTeamLead(getCellValue(row.getCell(12)));
                emp.setTeamLeadEmail(getCellValue(row.getCell(13)));

                // Set password only for new employees
                if (emp.getPassword() == null) {
                    emp.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                }

                employeeRepo.save(emp);
                log.info("Employee details saved/updated: {} (row {})", empId, i + 1);
            }

        } catch (Exception e) {
            log.error("Failed to upload employee details Excel", e);
            throw new RuntimeException("Failed to upload employee details Excel", e);
        }
    }

    // ================= HELPERS =================

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded Excel file is empty or null");
        }
    }

    private boolean isBlank(String... values) {
        for (String v : values) {
            if (v == null || v.trim().isEmpty()) return true;
        }
        return false;
    }

    private LocalDate parseDate(Cell cell) {
        String value = getCellValue(cell);
        return (value != null && !value.isBlank()) ? LocalDate.parse(value) : null;
    }

    private Boolean parseBoolean(Cell cell) {
        String value = getCellValue(cell);
        if (value == null) return null;
        return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd")
                            .format(cell.getDateCellValue());
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
}
    