package com.employeerating.serviceImpl;

import com.employeerating.dto.*;
import com.employeerating.dto.TaskSummaryDTO;
import com.employeerating.entity.Employee;
import com.employeerating.entity.EmployeeTask;
import com.employeerating.exception.EmployeeNotFoundException;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.repository.EmployeeTaskRepository;
import com.employeerating.service.EmployeeTaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
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

    // ================= SAVE TASKS =================

    @Override
    public void saveTasks(Employee employee, List<EmployeeTaskRequestDto> dtoList, List<MultipartFile> files) throws IOException {

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory at {}", uploadPath.toAbsolutePath());
        }

        Employee employee1 = employeeRepo.findByEmployeeId(employee.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employee.getEmployeeId()));

        LocalDate mainDate = dtoList.stream()
                .map(EmployeeTaskRequestDto::getDate)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Task date missing"));

        for (int i = 0; i < dtoList.size(); i++) {
            EmployeeTaskRequestDto dto = dtoList.get(i);
            LocalDate finalDate = dto.getDate() != null ? dto.getDate() : mainDate;

            Optional<EmployeeTask> existingTaskOpt =
                    taskRepository.findByTaskNameAndEmployeeIdAndWorkDate(
                            dto.getTaskName(),
                            employee1.getEmployeeId(),
                            finalDate
                    );

            EmployeeTask task = existingTaskOpt.orElse(new EmployeeTask());

            task.setEmployee(employee1);
            task.setEmployeeName(employee1.getEmployeeName());
            task.setEmployeeId(employee1.getEmployeeId());
            task.setDate(finalDate);
            task.setWorkDate(finalDate);
            task.setProjectNames(dto.getProjectName());
            task.setTaskName(dto.getTaskName());
            task.setDescription(dto.getDescription());
            task.setStatus(dto.getStatus());
            task.setPrLink(dto.getPrLink());

            // Team Lead mapping
            if (dto.getTeamLead() != null && !dto.getTeamLead().isBlank()) {
                List<Employee> leads = employeeRepo.findByEmployeeNameIgnoreCase(dto.getTeamLead());

                if (!leads.isEmpty()) {
                    Employee lead = leads.get(0);
                    task.setTeamLead(lead);
                    task.setTeamLeadId(lead.getEmployeeId());
                    task.setTeamLeadEmail(lead.getEmployeeEmail());

                    if (leads.size() > 1) {
                        log.warn("Multiple TLs found for name '{}'. Using {}", dto.getTeamLead(), lead.getEmployeeId());
                    }
                } else {
                    log.warn("Team lead not found for name '{}'", dto.getTeamLead());
                }
            }

            task.setTeamLeadName(dto.getTeamLead());

            // Hours handling
            LocalTime hrs = parseNormalizedToLocalTime(normalizeTime(dto.getHours()));
            LocalTime extra = parseNormalizedToLocalTime(normalizeTime(dto.getExtraHours()));

            task.setHours(hrs);
            task.setExtraHours(extra);
            task.setHoursSpent(toDecimalHours(hrs) + toDecimalHours(extra));

            // File upload
            if (files != null && i < files.size()) {
                MultipartFile file = files.get(i);
                if (!file.isEmpty()) {
                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    Files.copy(
                            file.getInputStream(),
                            Paths.get(uploadDir, fileName),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                    task.setFileName(fileName);
                }
            }

            taskRepository.save(task);
            log.debug("Saved task '{}' for employee {}", task.getTaskName(), employee1.getEmployeeId());
        }
    }

    // ================= FETCH =================

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

        List<EmployeeTask> tasks =
                taskRepository.findByTeamLeadIdAndWorkDate(teamLead.getEmployeeId(), date);

        if (tasks.isEmpty()) {
            return List.of(emptyResponse());
        }

        return mapTasks(tasks);
    }

    @Override
    public List<EmployeeTaskResponse> getListOfTasksByDateAndTlName(LocalDate date, String teamLeadName) {
    
        List<EmployeeTask> tasks =
                taskRepository.findUnratedTasksByTeamLeadName(teamLeadName, date);
    
        if (tasks.isEmpty()) {
            return List.of(emptyResponse());
        }
    
        return mapTasks(tasks);
    }

    @Override
    public List<EmployeeTaskResponse> getListOfTasksByDateAndEmployeeId(LocalDate date, String employeeId) {

        log.debug("Fetching tasks for employee {} on {}", employeeId, date);

        Employee employee = employeeRepo.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        List<EmployeeTask> tasks =
                taskRepository.findByEmployeeIdAndWorkDate(employee.getEmployeeId(), date);

        if (tasks.isEmpty()) {
            return List.of(emptyResponse());
        }

        return mapTasks(tasks);
    }

    @Override
    public List<TaskSummaryDTO> fetchTasksWithoutRating(String employeeId) {
        // Calculate current month's start and end dates
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1); // First day of current month
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth()); // Last day of current month
        
        // Call repository method to fetch unrated tasks
        return taskRepository.fetchTasksWithoutRating(employeeId, startDate, endDate);
    }

    // ================= PROCESS =================

    @Override
    public Map<String, Object> processTaskSubmission(String employeeId,
                                                     String tasksJson,
                                                     List<MultipartFile> files) {

        try {
            Employee employee = employeeRepo.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

            List<EmployeeTaskRequestDto> taskRequestList =
                    objectMapper.readValue(
                            tasksJson,
                            objectMapper.getTypeFactory()
                                    .constructCollectionType(List.class, EmployeeTaskRequestDto.class)
                    );

            saveTasks(employee, taskRequestList, files);

            return Map.of(
                    "status", "SUCCESS",
                    "message", "Tasks submitted successfully",
                    "employeeId", employeeId,
                    "taskCount", taskRequestList.size()
            );

        } catch (JsonProcessingException e) {
            log.error("Invalid JSON for tasks", e);
            return Map.of("status", "ERROR", "message", "Invalid JSON format");

        } catch (Exception e) {
            log.error("Task submission failed", e);
            return Map.of("status", "ERROR", "message", e.getMessage());
        }
    }

    // ================= UPDATE =================

    @Override
    @Transactional
    public TaskUpdateResponse updateTask(Long taskId, TaskUpdateRequest request) {

        EmployeeTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));

        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPrLink(request.getPrLink());
        task.setHours(parseNormalizedToLocalTime(normalizeTime(request.getHours())));
        task.setExtraHours(parseNormalizedToLocalTime(normalizeTime(request.getExtraHours())));

        EmployeeTask saved = taskRepository.save(task);

        return new TaskUpdateResponse(
                saved.getTaskId(),
                saved.getTaskName(),
                saved.getDescription(),
                saved.getStatus(),
                saved.getHours(),
                saved.getExtraHours(),
                saved.getPrLink(),
                saved.getWorkDate()
        );
    }

    // ================= HELPERS =================

    private double toDecimalHours(LocalTime time) {
        return time == null ? 0.0 : time.getHour() + time.getMinute() / 60.0;
    }

    private String normalizeTime(String time) {
        if (time == null || time.isBlank()) return null;
        return time.trim().toUpperCase().replace("AM", "").replace("PM", "").trim();
    }

    private LocalTime parseNormalizedToLocalTime(String normalized) {
        return normalized == null ? null
                : LocalTime.parse(normalized, DateTimeFormatter.ofPattern("H:mm"));
    }

    private EmployeeTaskResponse emptyResponse() {
        EmployeeTaskResponse response = new EmployeeTaskResponse();
        response.setEmployeeId("-");
        response.setEmployeeName("-");
        response.setTasks(List.of("No tasks are present"));
        return response;
    }

    private List<EmployeeTaskResponse> mapTasks(List<EmployeeTask> tasks) {

        Map<String, EmployeeTaskResponse> map = new HashMap<>();

        for (EmployeeTask task : tasks) {
            String empId = task.getEmployee() != null
                    ? task.getEmployee().getEmployeeId()
                    : task.getEmployeeId();

            EmployeeTaskResponse response = map.computeIfAbsent(empId, k -> {
                EmployeeTaskResponse r = new EmployeeTaskResponse();
                r.setEmployeeId(empId);
                r.setEmployeeName(
                        task.getEmployee() != null
                                ? task.getEmployee().getEmployeeName()
                                : task.getEmployeeName()
                );
                r.setTasks(new ArrayList<>());
                return r;
            });

            response.getTasks().add(task.getTaskName());
        }

        return new ArrayList<>(map.values());
    }

}

