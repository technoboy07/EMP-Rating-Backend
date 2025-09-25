package com.employeerating.entity;

import java.time.LocalDate;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long ratingId;

        private Double rating;
        private String remarks;
    @Column(name = "rating_date", nullable = false)
    private LocalDate ratingDate;
        private String ratedBy;

        private LocalDate tlSubmitDate;

        private LocalDate sendDateToPm;
        private Integer dailyRating;

        private LocalDate pmSubmitDate;
        private LocalDate pmoSubmitDate;
//        private String teamLeadId;
        private String teamLeadEmail;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "employee_id", nullable = false)
        @JsonIgnore // prevents infinite recursion
        private Employee employee;

    public Rating(LocalDate localDate, int i) {
    }

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "task_id", nullable = true)
//    private EmployeeTask task;
//    @OneToOne
//    @JoinColumn(name = "employee_id", nullable = false, unique = true)
//    private Employee employee;




//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Long id;
//
//	// Daily rating fields
//	private String employeeId; // Employee ID
//	private Integer dailyRating; // rating value (1-5)
//	private LocalDate ratingDate; // Date when rating was given
//	private String ratedBy; // Team lead email who gave the rating
//	private String employeeRemark; // Employee remark from team lead (nullable)
//
//	// Consolidated fields from EmployeeRatingTracker
//	private LocalDate sendDateToTL;
//
//	@Column(name="TeamLead_Submitted")
//	private LocalDate tlSubmitDate;
//
//	@Column(name="Send_Date_ProjectManager")
//	private LocalDate sendDateToPm;
//
//	@Column(name="ProjectManager_Submitted")
//	private LocalDate pmSubmitDate;
//
//	@Column(name="send_Date_ProjectManagerOffice")
//	private LocalDate sendDateToPmo;
//
//	@Column(name="ProjectManagerOfficer_submitted")
//	private LocalDate pmoSubmitDate;
//
//	@Column(name="send_To_Hr")
//	private LocalDate sendToHr;
//
//	private Boolean isSubmmited = false;
//
//	@OneToOne(mappedBy = "rating")
//	@JsonIgnore
//	private Employee employee;

}
