package com.employeerating.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamLeadDailyRatingDto {
    private LocalDate ratingDate;
    private List<EmployeeDailyRating> employeeRatings;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeDailyRating {
        private String employeeId;
        private Integer rating; // Single rating value (1-5)
        private String employeeRemark; // Employee remark from team lead (nullable)
        private Long taskId;
    }
}

