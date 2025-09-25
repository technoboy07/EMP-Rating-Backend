package com.employeerating.dto;

import lombok.Data;

import java.util.List;

@Data
public class RatingResponseByTeamLead {
    List<RatingTaskRequestDto> ratings;
}
