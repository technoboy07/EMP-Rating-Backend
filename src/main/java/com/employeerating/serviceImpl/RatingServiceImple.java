package com.employeerating.serviceImpl;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.employeerating.dto.RatingSubmissionDto;
import com.employeerating.dto.RatingTaskRequestDto;
import com.employeerating.dto.TaskResponse;
import com.employeerating.entity.EmployeeTask;
import com.employeerating.exception.EmployeeNotFoundException;
import com.employeerating.repository.EmployeeTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.employeerating.dto.TeamLeadDailyRatingDto;
import com.employeerating.entity.Employee;
import com.employeerating.entity.Rating;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.repository.RatingRepo;
import com.employeerating.service.RatingService;

import javax.transaction.Transactional;


@Service
public class RatingServiceImple implements RatingService {

    @Autowired
    RatingRepo ratingRepository;

    @Autowired
    EmployeeRepo employeeRepository;

    @Autowired
    EmployeeTaskRepository taskRepository;

    @Override
    @Transactional
    public void saveRatings(RatingSubmissionDto submission) {

        LocalDate ratingDate = submission.getDate();
        String teamLeadId = submission.getTeamLeadId();

        Employee teamLead = employeeRepository.findByEmployeeId(teamLeadId)
                .orElseThrow(() -> new EmployeeNotFoundException("Team Lead not found: " + teamLeadId));
        String teamLeadEmail = teamLead.getEmployeeEmail();

        for (RatingTaskRequestDto dto : submission.getEvaluations()) {

            String employeeId = dto.getEmployeeId();

            Employee employee = employeeRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + employeeId));

            Rating rating = ratingRepository.findByEmployeeAndRatingDate(employee,  ratingDate)
                    .orElse(null);

            if (rating == null) {
                rating = new Rating();
                rating.setEmployee(employee);
                //rating.setTask(task);
                rating.setRatingDate(ratingDate);
                rating.setRatedBy(teamLeadId);
            }

            rating.setRating(dto.getRating());
            rating.setRemarks(dto.getRemarks());
            rating.setTeamLeadEmail(teamLeadEmail);




            ratingRepository.save(rating);

//             Optional: update overall rating
            Double avg = ratingRepository.findAverageByEmployee(employee.getId());
            employee.setOverallRating(avg != null ? avg : 0);
            employeeRepository.save(employee);
        }
    }

/*
//my Imp
    public void saveRatings(List<RatingTaskRequestDto> ratingDtos) {


        for (RatingTaskRequestDto dto : ratingDtos) {
            // Find Employee (by employeeId in Employee table)

            Employee employee = employeeRepository.findByEmployeeId(dto.getEmployeeId())
                    .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + dto.getEmployeeId()));


            // Find Task (by taskName + employeeId instead of employeeName)
  *//*          List<EmployeeTask> tasks = taskRepository.findByTaskNameAndEmployeeId(
                    dto.getSelectedTasks(), dto.getEmployeeId()
            );*//*
//
//            if (tasks.isEmpty()) {
//                throw new RuntimeException("Task not found: " + dto.getSelectedTasks() +
//                        " for employeeId: " + dto.getEmployeeId());
//            }

// Example: pick the latest task by createdAt
//            EmployeeTask task = tasks.stream()
//                    .max(Comparator.comparing(EmployeeTask::getCreatedAt))
//                    .orElseThrow();

            // Create Rating
            Rating rating = new Rating();
            rating.setRating(dto.getRating());
            rating.setRemarks(dto.getRemarks());
            rating.setEmployee(employee);
//            rating.setTask(task);

            // Save Rating
            ratingRepository.save(rating);
        }
    }*/


    @Override
    public ResponseEntity<?> saveTeamLeadDailyRatings(TeamLeadDailyRatingDto dto, String teamLeadEmail) {
        List<String> saved = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Fetch all employees under this team lead
        List<Employee> employees = employeeRepository.findByTeamLeadEmail(teamLeadEmail);
        Map<String, Employee> employeeMap = employees.stream()
                .collect(Collectors.toMap(Employee::getEmployeeId, e -> e));

        for (TeamLeadDailyRatingDto.EmployeeDailyRating employeeRating : dto.getEmployeeRatings()) {
            String employeeId = employeeRating.getEmployeeId();
            Integer rating = employeeRating.getRating();
            String employeeRemark = employeeRating.getEmployeeRemark();
            Long taskId = employeeRating.getTaskId(); // assuming you added taskId to DTO
            try {
                taskId = Long.parseLong(employeeId);
            } catch (NumberFormatException e) {
                errors.add("Invalid employeeId: " + employeeId);
                continue;
            }

            // Validate employee belongs to team lead
            Employee employee = employeeMap.get(employeeId);
            if (employee == null) {
                errors.add("Employee " + employeeId + " does not belong to team lead " + teamLeadEmail);
                continue;
            }

            // Validate rating value (1-5)
            if (rating == null || rating < 1 || rating > 5) {
                errors.add("Invalid rating for employee " + employeeId + ": " + rating + " (must be 1-5)");
                continue;
            }

            // Fetch the task if provided
            EmployeeTask task = null;
            if (taskId != null) {
                task = taskRepository.findById(taskId)
                        .orElse(null); // if task not found, we can leave it null
                if (task == null) {
                    errors.add("Task not found with ID: " + taskId + " for employee " + employeeId);
                    continue;
                }
            }

            // Check if rating already exists for this employee and date
            Optional<Rating> existingRating = ratingRepository.findByEmployeeIdAndRatingDate(employeeId, dto.getRatingDate());
            Rating ratingEntity = existingRating.orElse(new Rating());

            ratingEntity.setEmployee(employee);
            ratingEntity.setDailyRating(rating);
            ratingEntity.setRatingDate(dto.getRatingDate());
            ratingEntity.setRatedBy(teamLeadEmail);
            ratingEntity.setRemarks(employeeRemark);

//            ratingEntity.setTask(task); // set task, can be null if not provided

            ratingRepository.save(ratingEntity);
            saved.add((existingRating.isPresent() ? "Updated" : "Created") + " rating for " + employeeId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("saved", saved);
        result.put("errors", errors);
        result.put("ratingDate", dto.getRatingDate());
        result.put("teamLeadEmail", teamLeadEmail);

        if (errors.isEmpty()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }


    @Override
    public ResponseEntity<?> getEmployeeRatingsByTeamLead(String teamLeadEmail) {
        List<Rating> ratings = ratingRepository.findByRatedByOrderByRatingDateDesc(teamLeadEmail);

        // Group ratings by date
        Map<LocalDate, List<Rating>> ratingsByDate = new HashMap<>();
        for (Rating rating : ratings) {
            ratingsByDate.computeIfAbsent(rating.getRatingDate(), k -> new ArrayList<>()).add(rating);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("teamLeadEmail", teamLeadEmail);
        result.put("ratingsByDate", ratingsByDate);
        result.put("totalRatings", ratings.size());

        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<?> getEmployeeRatingsByDate(LocalDate ratingDate) {
        List<Rating> ratings = ratingRepository.findByRatingDate(ratingDate);

        // Group ratings by team lead
        Map<String, List<Rating>> ratingsByTeamLead = new HashMap<>();
        for (Rating rating : ratings) {
            ratingsByTeamLead.computeIfAbsent(rating.getRatedBy(), k -> new ArrayList<>()).add(rating);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("ratingDate", ratingDate);
        result.put("ratingsByTeamLead", ratingsByTeamLead);
        result.put("totalRatings", ratings.size());

        return ResponseEntity.ok(result);
    }

    @Override
    public TaskResponse getTasksByTaskNamesandEmployeeIdAndDate(String taskNames, String employeeId, LocalDate workDate) {


        EmployeeTask entity = taskRepository
                .findByTaskNamesAndEmployeeIdAndWorkDate(taskNames, employeeId, workDate)
                .orElseThrow(() -> new RuntimeException(
                        "Task not found for employeeId " + employeeId + " on " + workDate));

        return mapToResponse(entity);
    }

    private TaskResponse mapToResponse(EmployeeTask task) {
        TaskResponse dto = new TaskResponse();
        dto.setId(task.getTaskId());
        dto.setDescription(task.getDescription());
        dto.setTask(task.getTaskName()); // if you want taskName as reference
        dto.setPrLink(task.getPrLink());
        dto.setStatus(task.getStatus());

        // Convert hours
        if (task.getHours() != null) {
            dto.setHours(task.getHours());
        }
        if (task.getExtraHours() != null) {
            dto.setExtraHours(task.getExtraHours());
        }

        return dto;

    }
}


