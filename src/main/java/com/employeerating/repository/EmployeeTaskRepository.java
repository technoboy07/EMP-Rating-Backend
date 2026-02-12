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

public interface 
EmployeeTaskRepository extends JpaRepository<EmployeeTask, Long> {

    Optional<EmployeeTask> findByTaskNameAndEmployeeIdAndWorkDate(String taskName, String employeeId, LocalDate workDate);

    List<EmployeeTask> findByEmployee(Employee employee);

// ... existing code above ...

@Query("SELECT t FROM EmployeeTask t " +
"WHERE LOWER(t.teamLeadName) = LOWER(:teamLeadName) " +
"ORDER BY t.createdAt ASC")
List<EmployeeTask> findByTeamLeadNameOrderByCreatedAtAsc(@Param("teamLeadName") String teamLeadName);

List<EmployeeTask> findByTeamLeadNameAndWorkDate(String teamLeadName, LocalDate workDate);

// List<EmployeeTask> findByTeamLeadEmployeeIdAndWorkDate(String teamLeadEmployeeIdName, LocalDate workDate);

// ... existing code below ...
    Optional<EmployeeTask> findByTaskNameAndEmployeeAndWorkDate(String taskName, Employee employee, LocalDate workDate);



    @Query("SELECT t FROM EmployeeTask t " +
            "WHERE t.taskName = :taskName " +
            "AND t.workDate = :workDate " +
            "AND t.employee.employeeId = :employeeId")
    Optional<EmployeeTask> findByTaskNamesAndEmployeeIdAndWorkDate(
            @Param("taskName") String taskNames,
            @Param("employeeId") String employeeId,
            @Param("workDate") LocalDate workDate
    );

    List<Employee>findByTeamLeadEmail(String teamLeadEmail);

    List<EmployeeTask> findByTaskNameAndEmployeeId(String taskName, String employeeId);


//    @Query("from EmployeeTask et where et.teamLead.employeeId = :teamLeadEmployeeId AND et.workDate = :workDate")
//    List<EmployeeTask> findByEmployeeIdAndWorkDate(String teamLeadEmployeeId, LocalDate workDate);


//    @Query("from EmployeeTask et where et.teamLeadId = :teamLeadId and et.workDate = :workDate")
//    List<EmployeeTask> findByEmployeeIdAndWorkDate(@Param("teamLeadId") String teamLeadId,
//                                                   @Param("workDate") LocalDate workDate);
//  
    
 // For Team Lead
    @Query("from EmployeeTask et where et.teamLeadId = :teamLeadId and et.workDate = :workDate")
    List<EmployeeTask> findByTeamLeadIdAndWorkDate(@Param("teamLeadId") String teamLeadId,
                                                   @Param("workDate") LocalDate workDate);

    // For Employee
    @Query("from EmployeeTask et where et.employeeId = :employeeId and et.workDate = :workDate")
    List<EmployeeTask> findByEmployeeIdAndWorkDate(@Param("employeeId") String employeeId,
                                                   @Param("workDate") LocalDate workDate);
    
    
    //Jaswanth
//    @Query("SELECT new com.employeerating.dto.TaskSummaryDTO(t.taskName, t.workDate) " +
// 	       "FROM EmployeeTask t WHERE t.employeeId = :employeeId " +
// 	       "AND t.taskId NOT IN (SELECT r.ratingId FROM Rating r WHERE r.employee.employeeId = :employeeId) " +
// 	       "AND t.workDate BETWEEN :startDate AND :endDate")
// 	List<TaskSummaryDTO> findTaskNamesAndDatesWithoutRating(String employeeId, LocalDate startDate, LocalDate endDate);

	
    
    //Robin
    @Query("SELECT new com.employeerating.dto.TaskSummaryDTO(t.taskId, t.taskName, t.workDate) " +
    	       "FROM EmployeeTask t " +
    	       "WHERE t.employeeId = :employeeId " +
    	       "AND t.workDate BETWEEN :startDate AND :endDate " +
    	       "AND NOT EXISTS ( " +
    	       "    SELECT 1 FROM Rating r " +
    	       "    WHERE r.employee.employeeId = :employeeId " +
    	       "    AND r.ratingDate = t.workDate " +
    	       ")")
    	List<TaskSummaryDTO> fetchTasksWithoutRating(
    	        @Param("employeeId") String employeeId,
    	        @Param("startDate") LocalDate startDate,
    	        @Param("endDate") LocalDate endDate
    	);




    
    

}
