package com.employeerating.repository;
 
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.employeerating.entity.EmailLog;
 
public interface EmailLogRepo extends JpaRepository<EmailLog, Long> {
    Optional<EmailLog> findByEmployeeIdAndSentDate(Long employeeId, LocalDate sentDate);
}	