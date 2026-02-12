package com.employeerating.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;


@Data
@JsonPropertyOrder({ "id", "task", "description", "prLink", "status", "hours", "extraHours" })
public class TaskResponse {
    private Long id;

    private String description;

    private String task;

    private String prLink;

    private String status;

    private LocalTime hours;

    private LocalTime extraHours;

}

