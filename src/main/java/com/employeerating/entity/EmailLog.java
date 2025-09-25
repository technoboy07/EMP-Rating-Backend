package com.employeerating.entity;
 
import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
 
@Entity
public class EmailLog {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private Long employeeId;
 
    private LocalDate sentDate;
 
    public EmailLog() {
    }
 
    public EmailLog(Long employeeId, LocalDate sentDate) {
        this.employeeId = employeeId;
        this.sentDate = sentDate;
    }
 
    public Long getId() {
        return id;
    }
 
    public void setId(Long id) {
        this.id = id;
    }
 
    public Long getEmployeeId() {
        return employeeId;
    }
 
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
 
    public LocalDate getSentDate() {
        return sentDate;
    }
 
    public void setSentDate(LocalDate sentDate) {
        this.sentDate = sentDate;
    }
}