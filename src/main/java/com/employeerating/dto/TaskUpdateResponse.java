package com.employeerating.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateResponse {

    private Long taskId;
    private String taskName;
    private String description;
    private String status;
    
    private LocalTime hours;
    private LocalTime extraHours;

    private String prLink;
    private LocalDate workDate;
}
