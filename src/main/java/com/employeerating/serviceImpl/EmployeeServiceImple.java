package com.employeerating.serviceImpl;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.employeerating.dto.EmployeeResponse;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.employeerating.dto.EmployeeDto;
import com.employeerating.dto.TeamLeadEmployeeDto;
import com.employeerating.entity.Employee;
import com.employeerating.entity.Rating;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.repository.RatingRepo;
import com.employeerating.service.EmployeeService;
import com.employeerating.util.ExcelGenerator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmployeeServiceImple implements EmployeeService {

	@Autowired
	EmployeeRepo employeeRepo;

	@Autowired
	RatingRepo ratingRepo;

	@Autowired
	ModelMapper mapper;

	@Autowired
	EntityManager entityManager;

    @Override
    public ResponseEntity<?> save(List<EmployeeDto> dto) {
        if (dto.isEmpty()) {
            return new ResponseEntity<>("Please fill the form", HttpStatus.NOT_ACCEPTABLE);
        }

        List<Employee> savingEmployee = dto.stream().map(employeeDto -> {
            Employee e = mapper.map(employeeDto, Employee.class);

            // Create default rating entry (optional)
            Rating rating = new Rating();
            rating.setEmployee(e);

            // Initialize and assign ratings list
            List<Rating> ratings = new ArrayList<>();
            ratings.add(rating);
            e.setRatings(ratings);

            return e;
        }).collect(Collectors.toList());

        List<Employee> savedEmployees = employeeRepo.saveAll(savingEmployee);

        List<EmployeeDto> returningDto = savedEmployees.stream()
                .map(returnDto -> mapper.map(returnDto, EmployeeDto.class))
                .collect(Collectors.toList());

        return new ResponseEntity<>(returningDto, HttpStatus.OK);
    }





    public EmployeeResponse fetchEmployeeById(String employeeId) {

        Employee e = employeeRepo.findAllByEmployeeId(employeeId);

        EmployeeResponse employeeResponse = new EmployeeResponse();
        employeeResponse.setEmployeeId(e.getEmployeeId());
        employeeResponse.setEmployeeName(e.getEmployeeName());
        employeeResponse.setRole(e.getEmployeeRole());
        return employeeResponse;
    }
	@Override
	public ResponseEntity<?> fetchAll() {
		List<Employee> employees = employeeRepo.findByNoticePeriodFalseAndProbationaPeriodFalse();
		return new ResponseEntity<List<Employee>>(employees, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> fetchAllByTeamLeadEmail(String teamLeadEmail) {
		List<Employee> employees = employeeRepo.findByTeamLeadEmail(teamLeadEmail);
		List<TeamLeadEmployeeDto> teamLeadEmployees = employees.stream()
			.map(employee -> new TeamLeadEmployeeDto(
				employee.getEmployeeId(),
				employee.getEmployeeName()
			))
			.collect(Collectors.toList());
		return new ResponseEntity<List<TeamLeadEmployeeDto>>(teamLeadEmployees, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getEmployee(LocalDate date) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
		Root<Employee> root = cq.from(Employee.class);
		cq.select(root).where(cb.equal(root.get("startDate"), date));
		TypedQuery<Employee> query = entityManager.createQuery(cq);
		List<Employee> result = query.getResultList();
		return new ResponseEntity<List<Employee>>(result, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<?> getByCriteria(String managerEmail) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
		Root<Employee> root = cq.from(Employee.class);
		cq.select(root).where(cb.equal(root.get("projectManagerEmail"), managerEmail));

		TypedQuery<Employee> query = entityManager.createQuery(cq);

		List<Employee> employees = query.getResultList();
		return new ResponseEntity<List<Employee>>(employees, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> deleteDetails(String empid) {
		Optional<Employee> savedEmployee = employeeRepo.findByEmployeeId(empid);
		if (savedEmployee.isEmpty())
			return new ResponseEntity<String>("No data found successfully", HttpStatus.NOT_FOUND);
		else {
			employeeRepo.deleteById(savedEmployee.get().getId());
			return new ResponseEntity<String>("Employee deleted successfully", HttpStatus.NOT_FOUND);
		}
	}


	@Override
	public byte[] generateEmployeeExcel(String employeeId) {
        Employee employee = employeeRepo.findByEmployeeId(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        try {
            return ExcelGenerator.generateExcelForEmployee(employee);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel: " + e.getMessage());
        }
    }

	@Override
	public byte[] generateEmployeesExcel(String projectManagerEmail) throws InvalidFormatException {
		List<Employee> employees = employeeRepo.findByProjectManagerEmail(projectManagerEmail);
		if(employees==null) {
			throw new RuntimeException("There is no employee");
		}
		try {
			return ExcelGenerator.generateExcelForEmployees(employees);
		} catch (IOException e) {
			throw new RuntimeException("Failed to generate Excel: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> updateEmployee(EmployeeDto dto) {
		Optional<Employee> savedEmployee = employeeRepo.findByEmployeeId(dto.getEmployeeId());
		if (savedEmployee.isEmpty())
			return new ResponseEntity<String>("No data found successfully", HttpStatus.NOT_FOUND);
		else {
			Employee employee = savedEmployee.get();
			employee.setEmployeeName(dto.getEmployeeName());
			employee.setEmployeeEmail(dto.getEmployeeEmail());
			employee.setProjectManagerName(dto.getProjectManagerName());
			employee.setProjectManagerEmail(dto.getProjectManagerEmail());
			employee.setProjectName(dto.getProjectName());
			employee.setStartDate(dto.getStartDate());
			employee.setEndDate(dto.getEndDate());
			employee.setTeamLead(dto.getTeamLead());
			employee.setTeamLeadEmail(dto.getTeamLeadEmail());
			employee.setPmoName(dto.getPmoName());
			employee.setPmoEmail(dto.getPmoEmail());
			employee.setDesignation(dto.getDesignation());
			employee.setDepartment(dto.getDepartment());
			employee.setEmploymentType(dto.getEmploymentType());
			employee.setJoiningDate(dto.getJoiningDate());
			employee.setLeaveDate(dto.getLeaveDate());
			employee.setNoticePeriod(dto.isNoticePeriod());
			employee.setProbationaPeriod(dto.isProbationaPeriod());
			
			// Set default values for authentication fields if not present
			if (employee.getPassword() == null || employee.getPassword().isEmpty()) {
				employee.setPassword("Rumango@123");
			}
			if (employee.getEmployeeRole() == null || employee.getEmployeeRole().isEmpty()) {
				employee.setEmployeeRole("Developer");
			}
			
			Employee saved = employeeRepo.save(employee);
			EmployeeDto returningDto = mapper.map(saved, EmployeeDto.class);
			return new ResponseEntity<EmployeeDto>(returningDto, HttpStatus.OK);
		}
	}

    @Override
    public ResponseEntity<?> saveSingleEmployee(EmployeeDto dto) {
        Employee employee = mapper.map(dto, Employee.class);

        // Set default values for authentication fields
        if (employee.getPassword() == null || employee.getPassword().isEmpty()) {
            employee.setPassword("Rumango@123");
        }
        if (employee.getEmployeeRole() == null || employee.getEmployeeRole().isEmpty()) {
            employee.setEmployeeRole("Developer");
        }

        // Initialize a new Rating and set relationship
        Rating rating = new Rating();
        rating.setEmployee(employee);

        // Initialize list of ratings and add the default rating
        List<Rating> ratings = new ArrayList<>();
        ratings.add(rating);
        employee.setRatings(ratings);

        // Save employee (cascade will save ratings too)
        Employee saved = employeeRepo.save(employee);

        EmployeeDto returningDto = mapper.map(saved, EmployeeDto.class);
        return new ResponseEntity<>(returningDto, HttpStatus.OK);
    }


    @Override
	public List<String> getAllEmployeeIds() {
		return employeeRepo.findAll().stream().map(e -> e.getEmployeeId()).collect(Collectors.toList());
	}
	
	@Override
	public ResponseEntity<?> save(com.employeerating.dto.FormData formData) {
		// Implementation for FormData save method
		return new ResponseEntity<>("FormData save method not implemented", HttpStatus.NOT_IMPLEMENTED);
	}

	@Override
	public byte[] generateEmployeesExcelForManagerOfficer(String managerOfficer) {
		// Implementation for manager officer Excel generation
		return new byte[0];
	}

	@Override
	public byte[] generateEmployeesExcelHr() {
		// Implementation for HR Excel generation
		return new byte[0];
    }



}



