package com.employeerating.serviceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import com.employeerating.dto.EmployeeTaskResponse;
import com.employeerating.exception.EmployeeNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.employeerating.dto.EmployeeTaskRequestDto;
import com.employeerating.entity.Employee;
import com.employeerating.entity.EmployeeTask;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.repository.EmployeeTaskRepository;
import com.employeerating.service.EmployeeTaskService;

@Service
public class EmployeeTaskServiceImp implements EmployeeTaskService {


    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private EmployeeTaskRepository taskRepository;

    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void saveTasks(Employee employee, List<EmployeeTaskRequestDto> dtoList, List<MultipartFile> files) throws IOException {
        // Ensure uploads folder exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Employee employee1 = employeeRepo.findByEmployeeId(employee.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employee.getEmployeeId()));

        for (int i = 0; i < dtoList.size(); i++) {
            EmployeeTaskRequestDto dto = dtoList.get(i);

            // 🔹 Check if task already exists (unique: taskName + employeeId + workDate)
            Optional<EmployeeTask> existingTaskOpt = taskRepository.findByTaskNameAndEmployeeIdAndWorkDate(
                    dto.getTaskName(),
                    employee1.getEmployeeId(),
                    dto.getDate() != null ? dto.getDate() : LocalDate.now()
            );

            EmployeeTask task = existingTaskOpt.orElse(new EmployeeTask());

            // 🔹 Basic mapping
            task.setEmployee(employee1);
            task.setEmployeeName(employee1.getEmployeeName());
            task.setEmployeeId(employee1.getEmployeeId());
            task.setDate(dto.getDate());
            task.setProjectNames(dto.getProjectName());
            task.setTaskName(dto.getTaskName());
            task.setDescription(dto.getDescription());
            task.setStatus(dto.getStatus());

            Employee teamLead=employeeRepo.findByEmployeeName(dto.getTeamLead());
            task.setTeamLeadEmail(teamLead.getEmployeeEmail());

            // 🔹 Handle LocalTime → double hours
            task.setHours(dto.getHours());
            task.setExtraHours(dto.getExtraHours());
            task.setHoursSpent(toDecimalHours(dto.getHours()) + toDecimalHours(dto.getExtraHours()));

            // 🔹 Map team lead
            if (dto.getTeamLead() != null && !dto.getTeamLead().isBlank()) {
                List<Employee> leadIds = employeeRepo.findByEmployeeNameIgnoreCase(dto.getTeamLead());

                if (!leadIds.isEmpty()) {
                    Employee employee2 = leadIds.get(0);
                    task.setTeamLeadId(employee2.getEmployeeId());
                    if (leadIds.size() > 1) {
                        System.out.println("Multiple team leads found for name: " + dto.getTeamLead() +
                                ". Picking employeeId=" + employee2.getEmployeeId());
                    }
                } else {
                    System.out.println("Team lead not found: " + dto.getTeamLead());
                }
            }

            task.setTeamLeadName(dto.getTeamLead());

            // 🔹 PR link
            task.setPrLink(dto.getPrLink());

            // 🔹 Work date (use DTO date or fallback to now)
            task.setWorkDate(dto.getDate() != null ? dto.getDate() : LocalDate.now());

            // 🔹 Handle file upload
            if (files != null && i < files.size()) {
                MultipartFile file = files.get(i);
                if (!file.isEmpty()) {
                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    Path path = Paths.get(uploadDir, fileName);
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                    task.setFileName(fileName);
                }
            }

            // 🔹 Save (insert or update)
            taskRepository.save(task);
        }
    }


    @Override
    public List<EmployeeTask> getListOfTask(String employeeId) {
        Employee employee = employeeRepo.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        return taskRepository.findByEmployee(employee);
    }


    @Override
    public List<EmployeeTaskResponse> getListOfTasksByDateAndTlId(LocalDate date, String employeeId) {
        Employee teamLead = employeeRepo.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Team lead not found: " + employeeId));

        List<EmployeeTask> tasks = taskRepository.findByEmployeeIdAndWorkDate((teamLead.getEmployeeId()), date);

        if (tasks.isEmpty()) {
            EmployeeTaskResponse response = new EmployeeTaskResponse();
            response.setEmployeeId("-");
            response.setEmployeeName("-");
            response.setTasks(List.of("No tasks are present"));
            return List.of(response);
        }

        Map<String, EmployeeTaskResponse> employeeMap = new HashMap<>();
        for (EmployeeTask task : tasks) {
            String empId = (task.getEmployee() != null) ? task.getEmployee().getEmployeeId() : task.getEmployeeId();
            String empName = (task.getEmployee() != null) ? task.getEmployee().getEmployeeName() : task.getEmployeeName();

            EmployeeTaskResponse response = employeeMap.get(empId);

            if (response == null) {
                response = new EmployeeTaskResponse();
                response.setEmployeeId(empId);
                response.setEmployeeName(empName);
                response.setTasks(new ArrayList<>());
                employeeMap.put(empId, response);
            }

            response.getTasks().add(task.getTaskName());
        }

        return new ArrayList<>(employeeMap.values());
    }

    private double toDecimalHours(LocalTime time) {
        if (time == null) return 0.0;
        return time.getHour() + time.getMinute() / 60.0;
    }

    @Override
    public Map<String, Object> processTaskSubmission(String employeeId, String tasksJson, List<MultipartFile> files) {
        try {
            Employee employee = employeeRepo.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee with employeeId " + employeeId + " not found"));

            List<EmployeeTaskRequestDto> taskRequestList = objectMapper.readValue(
                    tasksJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, EmployeeTaskRequestDto.class)
            );

            saveTasks(employee, taskRequestList, files);

            return Map.of(
                    "status", "SUCCESS",
                    "message", "Tasks submitted successfully",
                    "employeeId", employeeId,
                    "taskCount", taskRequestList.size()
            );

        } catch (IllegalArgumentException e) {
            return Map.of("status", "ERROR", "message", e.getMessage());
        } catch (JsonProcessingException e) {
            return Map.of("status", "ERROR", "message", "Invalid JSON format for tasks");
        } catch (IOException e) {
            return Map.of("status", "ERROR", "message", "File processing failed: " + e.getMessage());
        } catch (Exception e) {
            return Map.of("status", "ERROR", "message", "Unexpected error: " + e.getMessage());
        }
    }



}




