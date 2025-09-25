package com.employeerating.controller;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.employeerating.dto.EmployeeTaskResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.employeerating.entity.EmployeeTask;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.service.EmployeeTaskService;

import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/tasks")
@AllArgsConstructor
public class EmployeeTaskController {

    private EmployeeTaskService taskService;
    private EmployeeRepo employeeRepo;
    private ObjectMapper objectMapper;

    @PostMapping("/submit/{employeeId}")
    public ResponseEntity<?> submitTasks(
            @PathVariable String employeeId,
            @RequestPart(value = "tasks", required = false) String tasksJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        Map<String, Object> response = taskService.processTaskSubmission(employeeId, tasksJson, files);
        return ResponseEntity.ok(response);

    }



    @GetMapping("/{employeeId}")
    public ResponseEntity<?> getListOfTask(@PathVariable String employeeId) {
        List<EmployeeTask> tasks = taskService.getListOfTask(employeeId);

        if (tasks.isEmpty()) {
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Employee exists but has not submitted any tasks yet.",
                            "employeeId", employeeId,
                            "tasks", tasks
                    )
            );
        }

        return ResponseEntity.ok(tasks);
    }


    @GetMapping
    public ResponseEntity<List<EmployeeTask>> getAllTasks(String employeeId) {
        List<EmployeeTask> tasks = taskService.getListOfTask(employeeId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<EmployeeTaskResponse>> getTasksByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("employeeId") String employeeId) {
        System.out.println("inside request");
        List<EmployeeTaskResponse> tasks = taskService.getListOfTasksByDateAndTlId(date, employeeId);
        return ResponseEntity.ok(tasks);
    }

}
