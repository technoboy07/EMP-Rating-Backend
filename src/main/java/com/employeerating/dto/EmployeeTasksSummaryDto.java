package com.employeerating.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTasksSummaryDto {
    private String employeeId;
    private String employeeName;
    private List<TaskDto> entries; // one entry per day/task submission
    private java.util.Map<String, Double> dailyHours; // yyyy-MM-dd -> total hours for that day
    private Double totalHours;
}


