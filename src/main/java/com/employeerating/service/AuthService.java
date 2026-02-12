package com.employeerating.service;

import com.employeerating.dto.ForgotPasswordRequest;
import com.employeerating.dto.LoginRequest;
import com.employeerating.dto.LoginResponse;
import com.employeerating.dto.ResetPasswordRequest;


public interface AuthService {

  
    public LoginResponse login(LoginRequest request);
    
    String forgotPassword(ForgotPasswordRequest request);
    String resetPassword(ResetPasswordRequest request);
     
        
}
