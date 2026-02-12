package com.employeerating.controller;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.employeerating.dto.EmployeeTaskResponse;
import com.employeerating.dto.TaskSummaryDTO;
import com.employeerating.dto.TaskUpdateRequest;
import com.employeerating.dto.TaskUpdateResponse;
import com.employeerating.entity.EmployeeTask;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.service.EmployeeTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

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


    @GetMapping("/by-date-tlname")
    public ResponseEntity<List<EmployeeTaskResponse>> getTasksByDateAndTeamLeadName(
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam("teamLeadName") String teamLeadName) {

    List<EmployeeTaskResponse> tasks = taskService.getListOfTasksByDateAndTlName(date, teamLeadName);
    return ResponseEntity.ok(tasks);
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
        List<EmployeeTaskResponse> tasks = taskService.getListOfTasksByDateAndTlId(date, employeeId);
        return ResponseEntity.ok(tasks);
    }

    
    @GetMapping("/byemployee")
    public ResponseEntity<List<EmployeeTaskResponse>> getTasksByEmployeeAndDate(
            @RequestParam("employeeId") String employeeId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        System.out.println("inside employeeId + date request");

        List<EmployeeTaskResponse> tasks = taskService.getListOfTasksByDateAndEmployeeId(date, employeeId);

        if (tasks == null || tasks.isEmpty()) {
        	EmployeeTaskResponse response = new EmployeeTaskResponse();
        	response.setEmployeeId("-");
        	response.setEmployeeName("-");
        	response.setTasks(List.of("No tasks are present"));

        	return ResponseEntity.ok(List.of(response));  
        	}
        return ResponseEntity.ok(tasks);
    }

    
//	  Jaswanth    
//    @GetMapping("/without-rating/{employeeId}")
//    public ResponseEntity<?> getTasksWithoutRating(@PathVariable String employeeId) {
//        List<TaskSummaryDTO> tasks = taskService.getTasksWithoutRating(employeeId);
//        if (tasks.isEmpty()) {
//            return ResponseEntity.ok(Map.of(
//                "message", "No unrated tasks found for this employee in the current rating cycle.",
//                "tasks", tasks
//            ));
//        }
//        return ResponseEntity.ok(Map.of(
//            "message", "Unrated task names and dates fetched successfully.",
//            "tasks", tasks
//        ));
//    }

    
    //Robin
    @GetMapping("/withoutrating/{employeeId}")
    public ResponseEntity<?> fetchTasksWithoutRating(@PathVariable String employeeId) {

        List<TaskSummaryDTO> tasks = taskService.fetchTasksWithoutRating(employeeId);
        if (tasks.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "message", "No unrated tasks found for this employee in the current rating cycle.",
                    "tasks", tasks
            ));
        }
        return ResponseEntity.ok(Map.of(
                "message", "Unrated task names and dates fetched successfully.",
                "tasks", tasks
        ));
    }
    
    
    @PutMapping("/update/{taskId}")
    public ResponseEntity<TaskUpdateResponse> updateTask(
            @PathVariable Long taskId,
            @RequestBody TaskUpdateRequest request) {

        TaskUpdateResponse updatedTask = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(updatedTask);
    }


}
