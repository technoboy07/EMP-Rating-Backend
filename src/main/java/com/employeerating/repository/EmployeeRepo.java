package com.employeerating.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.employeerating.entity.Employee;
@Repository
public interface EmployeeRepo extends JpaRepository<Employee,Long>{
//    @Query("SELECT e FROM Employee e WHERE e.employeeId = :empId")
    Optional<Employee> findByEmployeeId(String empId);

	List<Employee> findByProjectManagerEmail(String projectManagerEmail);

	List<Employee> findByPmoEmail(String managerOfficer);
	
	List<Employee> findByTeamLeadEmail(String teamLeadEmail);
	
	List<Employee> findByNoticePeriodFalseAndProbationaPeriodFalse();
	
	List<Employee> findByTeamLeadEmailAndNoticePeriodFalseAndProbationaPeriodFalse(String teamLeadEmail);
	
	// Authentication methods (consolidated from UserRepository)
	Optional<Employee> findByEmployeeIdAndPassword(String employeeId, String password);
	
	boolean existsByEmployeeId(String employeeId);
    Employee findAllByEmployeeId(String employeeId);

    Employee findByEmployeeName(String teamLeadEmail);

//    Optional<Employee> findByEmployeeName(String employeeName);
//    @Query("SELECT e FROM Employee e WHERE e.employeeName = :name")
//    Optional<Employee> findByEmployeeName(String employeeName); // ✅ add this
    List<Employee> findByEmployeeNameIgnoreCase( String employeeName);

//    List<Employee>findByEmployeeName(String employeeName);



    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.ratings")
    List<Employee> findAllWithRatings();

    @Query("SELECT DISTINCT e.pmoEmail FROM Employee e WHERE LOWER(e.employeeRole) = 'pmo'")
    List<String> findDistinctPmoEmails();


}



