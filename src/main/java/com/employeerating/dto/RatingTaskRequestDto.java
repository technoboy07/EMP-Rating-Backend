package com.employeerating.dto;

import lombok.Data;



@Data
public class RatingTaskRequestDto {

    private String employeeId;
    private Double rating;
    private String remarks;


//    private String teamLeadEmployeeId;
//    private String employeeName;
//    private String selectedTasks;
}
