package com.employeerating.serviceImpl;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.employeerating.dto.EmployeeRatingTrackerDto;
import com.employeerating.entity.Employee;
import com.employeerating.entity.Rating;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.repository.RatingRepo;
import com.employeerating.service.EmployeeRatingTrackerService;

@Service
public class EmployeeRatingTrackerServiceImple implements EmployeeRatingTrackerService{
	
	@Autowired
	EmployeeRepo employeeRepo;
	
	@Autowired
	RatingRepo ratingRepo;
	
	@Autowired
	ModelMapper mapper;
	
	@Override
	public ResponseEntity<?> save(EmployeeRatingTrackerDto dto) {
		Rating rating = mapper.map(dto, Rating.class);
		Rating savedRating = ratingRepo.save(rating);
		EmployeeRatingTrackerDto savedDto = mapper.map(savedRating, EmployeeRatingTrackerDto.class);
		return new ResponseEntity<EmployeeRatingTrackerDto>(savedDto,HttpStatus.OK);
	}

    @Override
    public ResponseEntity<?> tlSubmit(String employeeId) {
        Employee savedEmployee = employeeRepo.findByEmployeeId(employeeId).get();

        if (savedEmployee.getRatings() != null && !savedEmployee.getRatings().isEmpty()) {
            // Get the latest rating
            Rating latestRating = savedEmployee.getRatings()
                    .get(savedEmployee.getRatings().size() - 1);

            latestRating.setTlSubmitDate(LocalDate.now());
            ratingRepo.save(latestRating);
        }

        return new ResponseEntity<>("TL Submitted Successfully", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> pmSubmit(String employeeId) {
        Employee savedEmployee = employeeRepo.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (savedEmployee.getRatings() != null && !savedEmployee.getRatings().isEmpty()) {
            savedEmployee.getRatings().forEach(rating -> {
                rating.setPmSubmitDate(LocalDate.now());
                ratingRepo.save(rating);
            });
        }

        return new ResponseEntity<>("PM Submitted Successfully", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> pmoSubmit(String employeeId) {
        Employee savedEmployee = employeeRepo.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (savedEmployee.getRatings() != null && !savedEmployee.getRatings().isEmpty()) {
            savedEmployee.getRatings().forEach(rating -> {
                rating.setPmoSubmitDate(LocalDate.now());
                ratingRepo.save(rating);
            });
        }

        return new ResponseEntity<>("PMO Submitted Successfully", HttpStatus.OK);
    }


}
