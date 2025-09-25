package com.employeerating.dto;

import lombok.Data;

import java.util.List;

@Data
public class EmployeeTaskResponse {


    private String employeeId;
    private String employeeName;
    private List<String> tasks;



}
