package com.employeerating.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.employeerating.entity.Employee;
import com.employeerating.entity.Rating;

public class ExcelGenerator {
    public static byte[] generateExcelInMemory(Employee employee) throws IOException {
        return new byte[0];
    }

    public static byte[] generateExcelForEmployee(Employee employee) throws IOException {
        return new byte[0];
    }


    // my impl
    public static byte[] generateExcelForEmployees(List<Employee> employees) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");

        // ===== Header Style =====
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // ===== Summary Row Style (bold + shaded) =====
        CellStyle summaryStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        summaryStyle.setFont(boldFont);
        summaryStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // ===== Header Row =====
        String[] columns = {"Employee ID", "Employee Name", "Date", "Rating", "Average Rating", "Score"};
        Row header = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 20 * 256);
        }

        int rowIdx = 1;
        for (Employee employee : employees) {
            List<Rating> ratings = employee.getRatings();
            if (ratings == null || ratings.isEmpty()) continue;

            // ===== Calculate actual average =====
            double actualAvg = ratings.stream().mapToDouble(r -> r.getRating() != null ? r.getRating() : 0).average().orElse(0);

            // ===== Floor value for display =====
            int floorAvg = (int) Math.floor(actualAvg);

            // ===== Calculate score based on actual average =====
            String score = actualAvg < 2 ? "0%" : actualAvg < 5 ? "50%" : "100%";

            // ===== One row per rating =====
            for (Rating r : ratings) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(employee.getEmployeeId());
                row.createCell(1).setCellValue(employee.getEmployeeName());
                row.createCell(2).setCellValue(r.getRatingDate() != null ? r.getRatingDate().toString() : "");
                row.createCell(3).setCellValue(r.getRating() != null ? r.getRating() : 0);
            }

            // ===== Summary Row =====
            Row summaryRow = sheet.createRow(rowIdx++);
            summaryRow.createCell(0).setCellValue(employee.getEmployeeId());
            summaryRow.createCell(1).setCellValue(employee.getEmployeeName());

            // Display floored average
            Cell avgCell = summaryRow.createCell(4);
            avgCell.setCellValue(floorAvg);
            avgCell.setCellStyle(summaryStyle);

            // Display score (calculated from actualAvg)
            Cell scoreCell = summaryRow.createCell(5);
            scoreCell.setCellValue(score);
            scoreCell.setCellStyle(summaryStyle);
        }

        // Auto-size columns
        for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            workbook.close();
            return bos.toByteArray();
        }
    }


    public static Map<String, byte[]> generateExcelPerTeamLead(List<Rating> ratings) throws IOException {
        // Group ratings by team lead email
        Map<String, List<Rating>> ratingsByTl = ratings.stream().filter(r -> r.getTeamLeadEmail() != null && !r.getTeamLeadEmail().isEmpty()).collect(Collectors.groupingBy(Rating::getTeamLeadEmail));

        Map<String, byte[]> excelPerTL = new HashMap<>();

        for (Map.Entry<String, List<Rating>> entry : ratingsByTl.entrySet()) {
            String tlEmail = entry.getKey();
            List<Rating> tlRatings = entry.getValue();

            // Group ratings back by employee for Excel
            Map<String, List<Rating>> ratingsByEmployee = tlRatings.stream().filter(r -> r.getEmployee() != null) // ensure relationship is present
                    .collect(Collectors.groupingBy(r -> r.getEmployee().getEmployeeId()));

            List<Employee> employees = ratingsByEmployee.values().stream().map(list -> {
                Employee emp = list.get(0).getEmployee();
                emp.setRatings(list); // attach only TL-specific ratings
                return emp;
            }).collect(Collectors.toList());

            byte[] excelBytes = generateExcelForEmployees(employees);
            excelPerTL.put(tlEmail, excelBytes);
        }

        return excelPerTL;
    }


    //by me
    public static byte[] generateExcelForEmployeesPM(List<Employee> employees) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");

        // ===== Header style =====
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // ===== Data style =====
        CellStyle dataStyle = workbook.createCellStyle();
        String[] columns = {"Employee_ID", "Employee_Name", "Average_Rating", "Score"};

        // ===== Header row =====
        Row header = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }


        // ===== Data rows =====
        int rowIdx = 1;
        for (Employee emp : employees) {
            Row row = sheet.createRow(rowIdx++);
            List<Rating> ratings = emp.getRatings();
            double actualAvg = 0;
            int floorAvg = 0;
            String score = "0%";
            if (ratings != null && !ratings.isEmpty()) {
                long distinctDays = ratings.stream()
                        .map(Rating::getRatingDate)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();
                if (distinctDays > 1) {
                    actualAvg = ratings.stream()
                            .mapToDouble(r -> r.getRating() != null ? r.getRating() : 0)
                            .average()
                            .orElse(0);
                    floorAvg = (int) Math.floor(actualAvg);
                    score = actualAvg < 2 ? "0%" : actualAvg < 5 ? "50%" : "100%";
                }

            }

            Cell cell0 = row.createCell(0);
            cell0.setCellValue(emp.getEmployeeId());
            cell0.setCellStyle(dataStyle);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(emp.getEmployeeName());
            cell1.setCellStyle(dataStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(floorAvg); // floor value
            cell2.setCellStyle(dataStyle);

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(score);
            cell3.setCellStyle(dataStyle);

        }

//        // ===== Remove any phantom extra cells =====
//        for (Row row : sheet) {
//            for (int c = columns.length; c < row.getLastCellNum(); c++) {
//                Cell cell = row.getCell(c);
//                if (cell != null) {
//                    row.removeCell(cell);
//                }
//            }
//        }


        // ===== Hybrid auto-size with minimum width =====
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i); // automatically size based on content
            int currentWidth = sheet.getColumnWidth(i); // get auto-sized width
            int minWidth = 20 * 256; // minimum width in characters
            if (currentWidth < minWidth) {
                sheet.setColumnWidth(i, minWidth);
            }
        }
                // ===== Write workbook to byte array =====

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            workbook.close();
            return bos.toByteArray();
        }
    }


    public static byte[] generateSimpleExcel() throws IOException {
        return new byte[0];
    }

    public static byte[] generateForProjectManager(List<Employee> employees) throws IOException, org.apache.poi.openxml4j.exceptions.InvalidFormatException {
        return new byte[0];
    }

    public static byte[] generateReadOnly(List<Employee> employees) throws IOException {
        return new byte[0];
    }
}
