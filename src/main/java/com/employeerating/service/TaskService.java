package com.employeerating.service;

import java.util.List;

import com.employeerating.dto.EmployeeTasksSummaryDto;

public interface TaskService {
    EmployeeTasksSummaryDto getTasksSummaryByEmployeeId(String employeeId);
    List<EmployeeTasksSummaryDto> getTasksSummariesByTeamLeadEmail(String teamLeadEmail);
}


