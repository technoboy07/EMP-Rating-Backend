package com.employeerating.service;

import java.time.LocalDate;
import java.util.List;

import com.employeerating.dto.RatingSubmissionDto;
import com.employeerating.dto.RatingTaskRequestDto;
import com.employeerating.dto.TaskResponse;
import org.springframework.http.ResponseEntity;

import com.employeerating.dto.TeamLeadDailyRatingDto;

public interface RatingService {


    // Old methods - commented out as per requirement
	/*
	ResponseEntity<?> save(RatingDto dto,String empid);

	Rating getRating(Long id);

	ResponseEntity<?> update(RatingDto dto, String employeeId);

	ResponseEntity<?> update(List<RatingDto> dtoList);

    ResponseEntity<?> getRatingsByEmployeeIds(List<String> employeeIds);
    ResponseEntity<?> bulkSaveRatings(List<RatingDto> dtos);
    */
    
    // New team lead daily rating methods
    ResponseEntity<?> saveTeamLeadDailyRatings(TeamLeadDailyRatingDto dto, String teamLeadEmail);

    ResponseEntity<?> getEmployeeRatingsByTeamLead(String teamLeadEmail);

    ResponseEntity<?> getEmployeeRatingsByDate(LocalDate ratingDate);

    TaskResponse getTasksByTaskNamesandEmployeeIdAndDate(String taskName, String employeeId, LocalDate workDate);

    void saveRatings(RatingSubmissionDto submission);



//    void saveRatings(List<RatingTaskRequestDto> ratingDtos);



}
