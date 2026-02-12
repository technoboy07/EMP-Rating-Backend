package com.employeerating.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class EmployeeTaskRequestDto {
    private LocalDate date;

    @JsonProperty("project")   
    private String projectName;

    @JsonProperty("taskTitle") 
    private String taskName;

    private String description;
    private String status;

    private String hours;
    private String extraHours;


    private String prLink;
    private String teamLead;
    private String fileName;
}



