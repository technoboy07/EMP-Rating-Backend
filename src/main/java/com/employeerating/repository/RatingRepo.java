package com.employeerating.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.employeerating.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employeerating.entity.Rating;

public interface RatingRepo extends JpaRepository<Rating, Long>{

//    Optional<Rating> findByEmployeeAndTaskAndRatingDate(Employee employee, LocalDate ratingDate);

    // Find ratings by employee ID and date
    Optional<Rating> findByEmployeeIdAndRatingDate(String employeeId, LocalDate ratingDate);
    
    // Find all ratings for an employee
//    List<Rating> findByEmployeeIdOrderByRatingDateDesc(String employeeId);
    
    // Find all ratings for a specific date
    List<Rating> findByRatingDate(LocalDate ratingDate);

    @Query("SELECT r FROM Rating r WHERE r.ratingDate < :date")
    List<Rating> findRatingsBeforeDate(@Param("date") LocalDate date);

    
    // Find all ratings given by a team lead
    List<Rating> findByRatedByOrderByRatingDateDesc(String ratedBy);


    List<Rating> findAllByEmployeeAndRatingDate(Employee employee, LocalDate ratingDate);


    Optional<Rating> findByEmployee(Employee employee);

    @Query("SELECT r FROM Rating r WHERE r.employee = :employee AND FUNCTION('DATE', r.ratingDate) = :ratingDate")
    Optional<Rating> findByEmployeeAndRatingDate(
            @Param("employee") Employee employee,
            @Param("ratingDate") LocalDate ratingDate);

//    Optional<Rating> findByEmployeeAndRatingDate(Employee employee, LocalDate ratingDate);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.employee.id = :employeeId")
    Double findAverageByEmployee(@Param("employeeId") Long employeeId);

    List<Rating>findByTeamLeadEmail(String teamLeadEmail);

}
