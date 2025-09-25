package com.employeerating.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;


@Data
public class RatingSubmissionDto {

    private String teamLeadId;
    private LocalDate date;
    private List<RatingTaskRequestDto> evaluations;
}
