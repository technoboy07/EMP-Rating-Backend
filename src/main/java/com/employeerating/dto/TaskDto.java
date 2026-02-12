package com.employeerating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private String task;
    private String status;
    private String date; // ISO yyyy-MM-dd
}


