package com.employeerating.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;

import com.employeerating.entity.Employee;
import com.employeerating.entity.EmployeeTask;
import com.employeerating.entity.Rating;

public class ExcelGenerator {
    public static byte[] generateExcelInMemory(Employee employee) throws IOException {
        return new byte[0];
        }


    public static byte[] generateExcelForEmployees(List<Employee> employees) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employee Ratings");

        // Header Style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Summary Row Style
        CellStyle summaryStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        summaryStyle.setFont(boldFont);
        summaryStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Columns
        String[] columns = {"Employee ID", "Employee Name", "Date", "Rating", "Average Rating", "Score"};
        Row header = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 20 * 256);
        }

        int rowIdx = 1;

        for (Employee emp : employees) {
            List<Rating> ratings = emp.getRatings();
            if (ratings == null || ratings.isEmpty()) continue;

            // Calculate actual average
            double actualAvg = ratings.stream()
                    .mapToDouble(r -> r.getRating() != null ? r.getRating() : 0)
                    .average()
                    .orElse(0);

            int flooredAvg = (int) Math.floor(actualAvg);

            String score = (actualAvg < 2) ? "0%" :
                           (actualAvg < 5) ? "50%" : "100%";

            // Rating rows
            for (Rating r : ratings) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(emp.getEmployeeId());
                row.createCell(1).setCellValue(emp.getEmployeeName());
                row.createCell(2).setCellValue(r.getRatingDate() != null ? r.getRatingDate().toString() : "");
                row.createCell(3).setCellValue(r.getRating() != null ? r.getRating() : 0);
            }

            // Summary Row
            Row sum = sheet.createRow(rowIdx++);
            sum.createCell(0).setCellValue(emp.getEmployeeId());
            sum.createCell(1).setCellValue(emp.getEmployeeName());

            Cell avgCell = sum.createCell(4);
            avgCell.setCellValue(flooredAvg);
            avgCell.setCellStyle(summaryStyle);

            Cell scoreCell = sum.createCell(5);
            scoreCell.setCellValue(score);
            scoreCell.setCellStyle(summaryStyle);
        }

        for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return bos.toByteArray();
    }

    public static Map<String, byte[]> generateExcelPerTeamLead(List<Rating> ratings) throws IOException {

        // Group ratings by TL email
        Map<String, List<Rating>> ratingsByTl = ratings.stream()
                .filter(r -> r.getTeamLeadEmail() != null && !r.getTeamLeadEmail().isEmpty())
                .collect(Collectors.groupingBy(Rating::getTeamLeadEmail));

        Map<String, byte[]> excelPerTL = new HashMap<>();

        for (Map.Entry<String, List<Rating>> entry : ratingsByTl.entrySet()) {
            String tlEmail = entry.getKey();
            List<Rating> tlRatings = entry.getValue();

            // Group the TL’s ratings by employee
            Map<Employee, List<Rating>> ratingsPerEmployee =
                    tlRatings.stream().collect(Collectors.groupingBy(Rating::getEmployee));

            List<Employee> employeeCopies = new ArrayList<>();

            for (Map.Entry<Employee, List<Rating>> empEntry : ratingsPerEmployee.entrySet()) {
                Employee original = empEntry.getKey();
                List<Rating> empRatings = empEntry.getValue();

                Employee copy = new Employee();
                BeanUtils.copyProperties(original, copy);
                copy.setRatings(empRatings);  // Only ratings given by this TL
                employeeCopies.add(copy);
            }

            byte[] excel = generateExcelForEmployees(employeeCopies);
            excelPerTL.put(tlEmail, excel);
        }

        return excelPerTL;
    }



    public static byte[] generateExcelForEmployeesPM(List<Employee> employees) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);

        CellStyle rightAlignStyle = workbook.createCellStyle();
        rightAlignStyle.setWrapText(true);
        rightAlignStyle.setAlignment(HorizontalAlignment.RIGHT);
        rightAlignStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        String[] columns = {"Employee ID", "Employee Name", "Average Rating", "Score", "Remark"};

        
        Row header = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);  // START: 1st of the month
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth()); // END: last day of the month

        int rowIdx = 1;
        for (Employee emp : employees) {
            Row row = sheet.createRow(rowIdx++);
            List<Rating> ratings = emp.getRatings();

            double actualAvg = 0;
            int floorAvg = 0;
            String score = "0%";

            if (ratings != null && !ratings.isEmpty()) {

                List<Rating> cycleRatings = ratings.stream()
                        .filter(r -> r.getRatingDate() != null)
                        .filter(r -> !r.getRatingDate().isBefore(startDate) &&
                                     !r.getRatingDate().isAfter(endDate))
                        .toList();

                long distinctDays = cycleRatings.stream()
                        .map(Rating::getRatingDate)
                        .distinct()
                        .count();

                if (distinctDays >= 15) {
                    actualAvg = cycleRatings.stream()
                            .mapToDouble(r -> r.getRating() != null ? r.getRating() : 0)
                            .average()
                            .orElse(0);
                    floorAvg = (int) Math.floor(actualAvg);
                    score = actualAvg < 2 ? "0%" : actualAvg < 5 ? "50%" : "100%";
                }
            }

            // ===== Write data to Excel =====
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(emp.getEmployeeId());
            cell0.setCellStyle(dataStyle);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(emp.getEmployeeName());
            cell1.setCellStyle(dataStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(floorAvg);
            cell2.setCellStyle(rightAlignStyle);

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(score);
            cell3.setCellStyle(rightAlignStyle);

            Cell cell4 = row.createCell(4);
            cell4.setCellValue("");
            cell4.setCellStyle(rightAlignStyle);
        }

        String[][] specialRows = {
                {"31359", "Piyush Merchant", "0", "0%", "Should be updated by Karthikeyan. C"},
                {"31251", "Calvin Clifford", "0", "0%", "Should be updated by Karthikeyan. C"},
                {"31072", "Alok Kumar Mohanty", "0", "0%", "Should be updated by Karthikeyan. C"}
        };

        for (String[] data : specialRows) {
            Row row = sheet.createRow(rowIdx++);
            for (int i = 0; i < data.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(data[i]);
                if (i >= 2) {
                    cell.setCellStyle(rightAlignStyle);
                } else {
                    cell.setCellStyle(dataStyle);
                }
            }
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            int minWidth = 20 * 256;
            if (currentWidth < minWidth) {
                sheet.setColumnWidth(i, minWidth);
            }
        }

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
    
    
    public static byte[] generateExcelForEmployee(
            Employee employee,
            LocalDate startDate,
            LocalDate endDate
    ) throws IOException {

        List<EmployeeTask> tasks = employee.getEmployeeTasks().stream()
                .filter(t -> !t.getWorkDate().isBefore(startDate)
                        && !t.getWorkDate().isAfter(endDate))
                .collect(Collectors.toList());

        List<Rating> ratings = employee.getRatings().stream()
                .filter(r -> r.getRatingDate() != null
                        && !r.getRatingDate().isBefore(startDate)
                        && !r.getRatingDate().isAfter(endDate))
                .collect(Collectors.toList());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(employee.getEmployeeName());

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        String[] headers = {"Date", "Tasks Submitted", "Remark", "Rating", "Rated By"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 25 * 256);
        }

        if (tasks == null || tasks.isEmpty()) {
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("No tasks found for this employee");
        } else {
            // Map ratings by date + rater
            Map<String, Rating> ratingMap = new HashMap<>();
            if (ratings != null) {
                for (Rating r : ratings) {
                    if (r.getRatingDate() != null && r.getRatedBy() != null) {
                        String key = r.getRatingDate() + "_" + r.getRatedBy();
                        ratingMap.put(key, r);
                    }
                }
            }

            // Group tasks by date + team lead
            Map<String, List<EmployeeTask>> groupedTasks = tasks.stream()
                    .collect(Collectors.groupingBy(task ->
                            task.getWorkDate() + "_" +
                                    (task.getTeamLeadId() != null ? task.getTeamLeadId() : "NA")
                    ));

            int rowIdx = 1;

            List<String> sortedKeys = groupedTasks.keySet().stream()
                    .sorted(Comparator.comparing(key ->
                            LocalDate.parse(key.split("_")[0])
                    ))
                    .collect(Collectors.toList());

            for (String key : sortedKeys) {
                List<EmployeeTask> dailyTasks = groupedTasks.get(key);
                if (dailyTasks == null || dailyTasks.isEmpty()) continue;

                LocalDate date = dailyTasks.get(0).getWorkDate();
                String teamLeadName = dailyTasks.get(0).getTeamLeadName() != null
                        ? dailyTasks.get(0).getTeamLeadName()
                        : "";

                Rating rating = ratingMap.get(key);

                // Concatenate task names
                String taskList = dailyTasks.stream()
                        .map(EmployeeTask::getTaskName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("\n"));

                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(dailyTasks.size() * sheet.getDefaultRowHeightInPoints());

                row.createCell(0).setCellValue(date.toString());

                Cell taskCell = row.createCell(1);
                taskCell.setCellValue(taskList);
                CellStyle wrapStyle = workbook.createCellStyle();
                wrapStyle.setWrapText(true);
                taskCell.setCellStyle(wrapStyle);

                if (rating != null) {
                    row.createCell(2).setCellValue(
                            rating.getRemarks() != null ? rating.getRemarks() : ""
                    );
                    row.createCell(3).setCellValue(rating.getRating() != null ? rating.getRating() : 0);
                    row.createCell(4).setCellValue(teamLeadName);
                } else {
                    row.createCell(2).setCellValue("");
                    row.createCell(3).setCellValue("");
                    row.createCell(4).setCellValue(teamLeadName);
                }
            }

            // --- Compute average rating and days rated per day ---
            Map<LocalDate, List<Rating>> ratingsByDate = ratings.stream()
                    .collect(Collectors.groupingBy(Rating::getRatingDate));

            double totalRating = 0;
            for (Map.Entry<LocalDate, List<Rating>> entry : ratingsByDate.entrySet()) {
                List<Rating> dailyRatings = entry.getValue();
                double dailyAvg = dailyRatings.stream()
                        .mapToDouble(r -> r.getRating() != null ? r.getRating() : 0)
                        .average()
                        .orElse(0);
                totalRating += dailyAvg;
            }

            int ratingCount = ratingsByDate.size(); // unique days rated
            double avgRating = ratingCount > 0 ? totalRating / ratingCount : 0;
            String score = (avgRating < 2) ? "0%" :
                           (avgRating < 5 ? "50%" : "100%");

            // Summary style
            CellStyle summaryStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            summaryStyle.setFont(boldFont);

            // Average Rating row
            Row avgRow = sheet.createRow(rowIdx + 1);
            avgRow.createCell(1).setCellValue("Average Rating");
            Cell avgValCell = avgRow.createCell(2);
            avgValCell.setCellValue(String.format("%.2f", avgRating));
            avgValCell.setCellStyle(summaryStyle);

            // Score row
            Row scoreRow = sheet.createRow(rowIdx + 2);
            scoreRow.createCell(1).setCellValue("Score");
            Cell scoreValCell = scoreRow.createCell(2);
            scoreValCell.setCellValue(score);
            scoreValCell.setCellStyle(summaryStyle);

            // Days Rated row
            Row countRow = sheet.createRow(rowIdx + 3);
            countRow.createCell(1).setCellValue("Days Rated");
            Cell daysValCell = countRow.createCell(2);
            daysValCell.setCellValue(ratingCount);
            daysValCell.setCellStyle(summaryStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            workbook.close();
            return bos.toByteArray();
        }
    }


}
 
