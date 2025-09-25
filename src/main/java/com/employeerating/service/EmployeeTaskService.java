package com.employeerating.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.employeerating.dto.EmployeeTaskResponse;
import org.springframework.web.multipart.MultipartFile;

import com.employeerating.dto.EmployeeTaskRequestDto;
import com.employeerating.entity.Employee;
import com.employeerating.entity.EmployeeTask;

public interface EmployeeTaskService {
    void saveTasks(Employee employee,List<EmployeeTaskRequestDto> dtoList, List<MultipartFile> files) throws IOException;
    List<EmployeeTask> getListOfTask(String employeeId);
    List<EmployeeTaskResponse> getListOfTasksByDateAndTlId(LocalDate date,String teamLeadEmployeeId);
    Map<String, Object> processTaskSubmission(String employeeId, String tasksJson, List<MultipartFile> files);

}