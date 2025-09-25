package com.employeerating.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.employeerating.dto.RatingSubmissionDto;
import com.employeerating.dto.TaskResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.employeerating.dto.TeamLeadDailyRatingDto;
import com.employeerating.service.RatingService;

@RestController
@RequestMapping("/rating")
public class RatingController {

	@Autowired
	RatingService ratingService;

	// Old endpoints - commented out as per requirement
	/*
	@PostMapping("/save/{empid}")
	public ResponseEntity<?> save(@RequestBody RatingDto dto,
			@PathVariable(name = "empid", required = true) String empid) {
		return ratingService.save(dto, empid);
	}

	@GetMapping("/getRating/{id}")
	public Rating getRating(@PathVariable(name = "id", required = true) Long id) {
		return ratingService.getRating(id);
	}

	@PostMapping(value = "/pmupdate/{employeeId}", consumes = "application/json")
	public ResponseEntity<?> update(@RequestBody RatingDto dto,@PathVariable String employeeId) {
		return ratingService.update(dto,employeeId);
	}
	@PostMapping("/pmupdateall")
	public ResponseEntity<?> updateAll(@RequestBody List<RatingDto> dtoList) {
		return ratingService.update(dtoList);
//	    return ratingService.saveAll(dtoList);
	}
	@PostMapping("/bulkSave")
	public ResponseEntity<?> bulkSave(@RequestBody List<RatingDto> dtos) {
		return ratingService.bulkSaveRatings(dtos);
	}
	*/

    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitRatings(@RequestBody RatingSubmissionDto submission) {
        ratingService.saveRatings(submission);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Ratings submitted successfully!");
        return ResponseEntity.ok(response);
    }


//    @PostMapping("/submit")
//    public ResponseEntity<String> submitRatings(@RequestBody List<RatingTaskRequestDto> ratings) {
//        ratingService.saveRatings(ratings);
//        return ResponseEntity.ok("Ratings submitted successfully!");
//    }
	// New team lead daily rating endpoints
	@PostMapping("/teamlead/daily/{teamLeadEmail}")
	public ResponseEntity<?> saveTeamLeadDailyRatings(
			@RequestBody TeamLeadDailyRatingDto dto,
			@PathVariable String teamLeadEmail) {
		return ratingService.saveTeamLeadDailyRatings(dto, teamLeadEmail);
	}
	
	@GetMapping("/teamlead/{teamLeadEmail}")
	public ResponseEntity<?> getEmployeeRatingsByTeamLead(@PathVariable String teamLeadEmail) {
		return ratingService.getEmployeeRatingsByTeamLead(teamLeadEmail);
	}
	
	@GetMapping("/date/{ratingDate}")
	public ResponseEntity<?> getEmployeeRatingsByDate(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ratingDate) {
		return ratingService.getEmployeeRatingsByDate(ratingDate);
	}

    @GetMapping("/getTasks")
    public ResponseEntity<TaskResponse> getTasksByTaskNamesandEmployeeIdAndDate(
            @RequestParam(name = "taskNames") String taskNames,
            @RequestParam(name = "employeeId")  String employeeId,
            @RequestParam(name = "workDate")@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate){

        TaskResponse task = ratingService.getTasksByTaskNamesandEmployeeIdAndDate(taskNames, employeeId, workDate);
        return ResponseEntity.ok(task);
    }


}