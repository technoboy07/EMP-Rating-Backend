package com.employeerating.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_tasks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"task_name", "employee_id","work_date"}),
indexes = {
        @Index(name = "idx_employee_tasks_employee_id", columnList = "employee_Id")
})

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EmployeeTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @Column(name = "employee_name")
    private String employeeName;
    @Column(name = "employe_id")
    private String employeeId;
    private LocalDate date = LocalDate.now();
    @Column(name = "project_name")
    private String projectNames;
    @Column(name = "task_name")
    private String taskName;
    @Column(name = "team_lead")
    private String teamLeadName;

    @Column(length = 1000)
    private String description;

    private String status;
    private LocalTime hours;
    private LocalTime extraHours;
    private String fileName;

    @Column(name = "hours_spent", nullable = false)
    private Double hoursSpent = 0.0;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate = LocalDate.now();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(length = 1000)
    private String prLink;

    private String teamLeadId;
    private String teamLeadEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teamlead_id")
    @JsonIgnore
    private Employee teamLead;


}
