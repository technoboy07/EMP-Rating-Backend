package com.employeerating.serviceImpl;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.employeerating.dto.ForgotPasswordRequest;
import com.employeerating.dto.LoginRequest;
import com.employeerating.dto.LoginResponse;
import com.employeerating.dto.ResetPasswordRequest;
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
    @Autowired
    private OtpService otpService;
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
//        if (!employee.getPassword().equals(request.getPassword())) {
//            log.error("Invalid password for employeeId: {}", request.getEmployeeId());
//            throw new InvalidCredentialsException("Invalid credentials");
//        }

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
    
    
    public String forgotPassword(ForgotPasswordRequest request){
        Employee emp = employeeRepo.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        otpService.generateAndSendOtp(emp.getEmployeeId(), emp.getEmployeeEmail());
        return "OTP sent to registered email";
    }

    public String resetPassword(ResetPasswordRequest request) {
        Stream.of(request)
                .filter(r ->
                        r.getNewPassword().equals(r.getConformPassword()))
                .findFirst()
                .orElseThrow(()-> new RuntimeException("New password and confirm password do not match"));
        	Employee employee= employeeRepo.findAllByEmployeeId(request.getEmployeeId());
        	if (passwordEncoder.matches(request.getConformPassword(), employee.getPassword()))
        		throw new InvalidCredentialsException("Previous password and current password are same, enter a different password");
        		
        		
        Employee emp = employeeRepo.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        emp.setPassword(passwordEncoder.encode(request.getConformPassword()));
        employeeRepo.save(emp);
        otpService.clearOtp(request.getEmployeeId());
        return "Password reset successful";
    }
 
	

}