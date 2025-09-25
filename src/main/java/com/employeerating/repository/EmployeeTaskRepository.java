package com.employeerating.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.employeerating.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import com.employeerating.entity.EmployeeTask;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeTaskRepository extends JpaRepository<EmployeeTask, Long> {

    Optional<EmployeeTask> findByTaskNameAndEmployeeIdAndWorkDate(String taskName, String employeeId, LocalDate workDate);

    List<EmployeeTask> findByEmployee(Employee employee);

    //    @Query("SELECT t FROM EmployeeTask t WHERE t.employee.employeeId = :employeeId ORDER BY t.createdAt ASC")
    List<EmployeeTask> findByEmployeeEmployeeIdOrderByCreatedAtAsc(@Param("employeeId") String employeeId);
    List<EmployeeTask> findByTeamLeadNameAndWorkDate(String teamLeadName, LocalDate workDate);
//    List<EmployeeTask> findByTeamLeadEmployeeIdAndWorkDate(String teamLeadEmployeeIdName, LocalDate workDate);

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



    // current chage

    @Query("from EmployeeTask et where et.teamLeadId = :teamLeadId and et.workDate = :workDate")
    List<EmployeeTask> findByEmployeeIdAndWorkDate(@Param("teamLeadId") String teamLeadId,
                                                   @Param("workDate") LocalDate workDate);
}
