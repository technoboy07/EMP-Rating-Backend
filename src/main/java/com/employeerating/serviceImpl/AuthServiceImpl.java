package com.employeerating.serviceImpl;

import com.employeerating.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.employeerating.dto.LoginRequest;
import com.employeerating.dto.LoginResponse;
import com.employeerating.entity.Employee;
import com.employeerating.exception.InvalidCredentialsException;
import com.employeerating.exception.UserNotFoundException;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.service.AuthService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for employeeId: {}", request.getEmployeeId());

        Employee employee = employeeRepo.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> {
                    log.error("User not found for employeeId: {}", request.getEmployeeId());
                    return new UserNotFoundException("User not found");
                });

        log.info("User found: {}", employee.getEmployeeId());

        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            log.error("Invalid password for employeeId: {}", request.getEmployeeId());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Store employeeId in session
//        SessionUtils.setEmployeeId(employee.getEmployeeId());

        // Redirect only for Developer and Team Lead
        String redirectUrl = null;
        String email = null;
        switch (employee.getEmployeeRole().toLowerCase()) {
            case "developer":
                redirectUrl = "https://employee-task-summary.vercel.app/";
                break;
            case "team lead", "teamlead":
                redirectUrl = "https://team-lead-rating.vercel.app/";
                email = employee.getEmployeeEmail();
                break;
            default:
                redirectUrl = null;
                break;
        }

        return new LoginResponse(
                "Login successful",
                employee.getEmployeeRole(),
                employee.getEmployeeId(),
                employee.getEmployeeName(),
                redirectUrl,
                email
        );
    }

}