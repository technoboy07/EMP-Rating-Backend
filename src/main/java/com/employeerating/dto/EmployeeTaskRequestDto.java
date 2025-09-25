package com.employeerating.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class EmployeeTaskRequestDto {
    private LocalDate date;

    @JsonProperty("project")   // Angular sends "project"
    private String projectName;

    @JsonProperty("taskTitle") // Angular sends "taskTitle"
    private String taskName;

    private String description;
    private String status;

    // Angular sends as string "HH:mm", need @JsonFormat
    @JsonFormat(pattern = "HH:mm")
    private LocalTime hours;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime extraHours;

    private String prLink;
    private String teamLead;
    private String fileName;
}

