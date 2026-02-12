package com.employeerating.dto;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateRequest {
    private String description;
    private String status;
    private String hours;
    private String extraHours;

    private String prLink;
}
