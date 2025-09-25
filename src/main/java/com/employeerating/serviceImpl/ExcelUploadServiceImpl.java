package com.employeerating.serviceImpl;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.employeerating.entity.Employee;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.service.ExcelUploadService;

@Service
public class ExcelUploadServiceImpl implements ExcelUploadService {

    @Autowired
    private EmployeeRepo employeeRepo;

    @Override

    public void uploadExcel(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            // Skip header (row 0)
            System.out.println("Total rows in sheet: " + sheet.getLastRowNum());
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    System.out.println("Row " + (i + 1) + " is null, skipping");
                    continue;
                }

                // Read from correct columns: B=1, C=2, D=3, E=4 (0-indexed)
                Cell cellB = row.getCell(1); // Emp ID
                Cell cellC = row.getCell(2); // Name
                Cell cellD = row.getCell(3); // Role
                Cell cellE = row.getCell(4); // Email
                String empId = getCellValue(cellB);
                String name = getCellValue(cellC);
                String role = getCellValue(cellD);
                String email = getCellValue(cellE);

                // Debug for first few rows
                if (i <= 5) {
                    System.out.println("Row " + (i + 1) + " - EmpID: [" + empId +
                            "], Name: [" + name +
                            "], Role: [" + role +
                            "], Email: [" + email + "]");
                }

                // Validate required fields BEFORE creating employee object
                if (empId == null || empId.trim().isEmpty() ||
                        name == null || name.trim().isEmpty() ||
                        role == null || role.trim().isEmpty() ||
                        email == null || email.trim().isEmpty()) {
                    System.out.println("Row " + (i + 1) + ": Skipping row with empty values - EmpID: [" +
                            empId + "], Name: [" + name + "], Role: [" + role + "], Email: [" + email + "]");
                    continue;
                }

                // Check if employee ID already exists
                if (employeeRepo.findByEmployeeId(empId.trim()).isPresent()) {
                    System.out.println("Row " + (i + 1) + ": Employee ID " + empId + " already exists - skipping");
                    continue;
                }
                try {
                    // Create new Employee only after validation passes
                    Employee employee = new Employee();
                    employee.setEmployeeId(empId.trim());
                    employee.setEmployeeName(name.trim());
                    employee.setPassword("Rumango@123");  // default password
                    employee.setEmployeeRole(role.trim());
                    employee.setEmployeeEmail(email.trim()); // <-- from Excel
                    // Set valid default values for required fields (NOT NULL constraints)
                    employee.setProjectManagerName("TBD");
                    employee.setProjectManagerEmail("pm@company.com");
                    // Set nullable Boolean fields to null (not false)
                    employee.setPmSubmitted(null);
                    employee.setIsTLSubmitted(null);
                    employee.setIsHrSend(null);
                    employee.setIsPmoSubmitted(null);
                    employee.setNoticePeriod(null);
                    employee.setProbationaPeriod(null);
                    employeeRepo.save(employee);
                    System.out.println("Row " + (i + 1) + ": Successfully saved employee " + empId);
                } catch (Exception e) {
                    System.out.println("Row " + (i + 1) + ": Failed to save employee " + empId + " - " + e.getMessage());
                    throw new RuntimeException("Failed to save employee " + empId + " at row " + (i + 1) + ": " + e.getMessage());
                }
            }
            workbook.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }
    }


    @Override
    public void uploadEmployeeDetailsExcel(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            System.out.println("📄 Total rows found: " + sheet.getLastRowNum());

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String empId = getCellValue(row.getCell(1));
                String name = getCellValue(row.getCell(2));
                String role = getCellValue(row.getCell(3));
                String email = getCellValue(row.getCell(4));
                String employmentType = getCellValue(row.getCell(5));
                String joiningDateStr = getCellValue(row.getCell(6));
                String leaveDateStr = getCellValue(row.getCell(7));
                String noticePeriodStr = getCellValue(row.getCell(8));
                String probationStr = getCellValue(row.getCell(9));
                String pmEmail = getCellValue(row.getCell(10));
                String projectName = getCellValue(row.getCell(11));
                String teamLead = getCellValue(row.getCell(12));
                String teamLeadEmail = getCellValue(row.getCell(13));

                if (empId == null || empId.trim().isEmpty()) continue;
                if (employeeRepo.findByEmployeeId(empId.trim()).isPresent()) continue;

                LocalDate joiningDate = (joiningDateStr != null && !joiningDateStr.isEmpty()) ? LocalDate.parse(joiningDateStr) : null;

                LocalDate leaveDate = (leaveDateStr != null && !leaveDateStr.isEmpty()) ? LocalDate.parse(leaveDateStr) : null;

                Boolean noticePeriod = (noticePeriodStr != null && (noticePeriodStr.equalsIgnoreCase("Yes") || noticePeriodStr.equalsIgnoreCase("True")));

                Boolean probationaPeriod = (probationStr != null && (probationStr.equalsIgnoreCase("Yes") || probationStr.equalsIgnoreCase("True")));

                Employee emp = new Employee();
                emp.setEmployeeId(empId.trim());
                emp.setEmployeeName(name);
                emp.setEmployeeRole(role);
                emp.setEmployeeEmail(email);
                emp.setEmploymentType(employmentType);
                emp.setJoiningDate(joiningDate);
                emp.setLeaveDate(leaveDate);
                emp.setNoticePeriod(noticePeriod);
                emp.setProbationaPeriod(probationaPeriod);
                emp.setProjectManagerEmail(pmEmail);
                emp.setProjectManagerName("TBD");
                emp.setProjectName(projectName);
                emp.setTeamLead(teamLead);
                emp.setTeamLeadEmail(teamLeadEmail);
                emp.setPassword("Rumango@123");

                employeeRepo.save(emp);
                System.out.println("✅ Saved employee row " + (i + 1) + ": " + empId);
            }

            workbook.close();
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to parse Employee Details Excel: " + e.getMessage(), e);
        }
    }

    // 🔹 Helper method to safely read any cell type
    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    return new SimpleDateFormat("yyyy-MM-dd").format(date);
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return null;
            default:
                return cell.toString().trim();
        }
    }
}

