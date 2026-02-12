package com.employeerating.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employeerating.dto.TaskSummaryDTO;
import com.employeerating.entity.Employee;
import com.employeerating.entity.EmployeeTask;

public interface EmployeeTaskRepository extends JpaRepository<EmployeeTask, Long> {

    // Find a task by name, employeeId and work date (used when saving/updating)
    Optional<EmployeeTask> findByTaskNameAndEmployeeIdAndWorkDate(String taskName, String employeeId, LocalDate workDate);

    // All tasks for a given Employee entity
    List<EmployeeTask> findByEmployee(Employee employee);

    // All tasks for an employee by employeeId, ordered by createdAt (used by TaskServiceImple)
    @Query("SELECT t FROM EmployeeTask t " +
           "WHERE t.employee.employeeId = :employeeId " +
           "ORDER BY t.createdAt ASC")
    List<EmployeeTask> findByEmployeeEmployeeIdOrderByCreatedAtAsc(@Param("employeeId") String employeeId);

    // All tasks for a given Team Lead name, ordered by createdAt (name-based TL view)
    @Query("SELECT t FROM EmployeeTask t " +
           "WHERE LOWER(t.teamLeadName) = LOWER(:teamLeadName) " +
           "ORDER BY t.createdAt ASC")
    List<EmployeeTask> findByTeamLeadNameOrderByCreatedAtAsc(@Param("teamLeadName") String teamLeadName);

    // Tasks for a given Team Lead name on a specific date (name-based TL + date)
    List<EmployeeTask> findByTeamLeadNameAndWorkDate(String teamLeadName, LocalDate workDate);

    // Task by name + Employee entity + date
    Optional<EmployeeTask> findByTaskNameAndEmployeeAndWorkDate(String taskName, Employee employee, LocalDate workDate);

    // Task by name + employeeId + date (used by RatingServiceImple.getTasksByTaskNamesandEmployeeIdAndDate)
    @Query("SELECT t FROM EmployeeTask t " +
           "WHERE t.taskName = :taskName " +
           "AND t.workDate = :workDate " +
           "AND t.employee.employeeId = :employeeId")
    Optional<EmployeeTask> findByTaskNamesAndEmployeeIdAndWorkDate(
            @Param("taskName") String taskNames,
            @Param("employeeId") String employeeId,
            @Param("workDate") LocalDate workDate
    );

    // Team leads by email (kept for existing usage)
    List<Employee> findByTeamLeadEmail(String teamLeadEmail);

    // Tasks by task name + employeeId (helper)
    List<EmployeeTask> findByTaskNameAndEmployeeId(String taskName, String employeeId);

    // For Team Lead (ID-based TL view)
    @Query("from EmployeeTask et where et.teamLeadId = :teamLeadId and et.workDate = :workDate")
    List<EmployeeTask> findByTeamLeadIdAndWorkDate(@Param("teamLeadId") String teamLeadId,
                                                   @Param("workDate") LocalDate workDate);

    // For Employee (ID-based employee view)
    @Query("from EmployeeTask et where et.employeeId = :employeeId and et.workDate = :workDate")
    List<EmployeeTask> findByEmployeeIdAndWorkDate(@Param("employeeId") String employeeId,
                                                   @Param("workDate") LocalDate workDate);

    // Unrated tasks for current cycle (used by EmployeeTaskController.fetchTasksWithoutRating)
    @Query("SELECT new com.employeerating.dto.TaskSummaryDTO(t.taskId, t.taskName, t.workDate) " +
    "FROM EmployeeTask t " +
    "WHERE t.employeeId = :employeeId " +
    "AND t.workDate BETWEEN :startDate AND :endDate " +
    "AND NOT EXISTS ( " +
    "    SELECT 1 FROM Rating r " +
    "    JOIN Employee tl ON tl.employeeId = r.ratedBy " +
    "    WHERE r.employee.employeeId = :employeeId " +
    "    AND r.ratingDate = t.workDate " +
    "    AND LOWER(tl.employeeName) = LOWER(t.teamLeadName) " +
    ")")
    
    List<TaskSummaryDTO> fetchTasksWithoutRating(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}