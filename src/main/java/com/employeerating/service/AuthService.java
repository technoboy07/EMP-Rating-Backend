package com.employeerating.service;

import com.employeerating.dto.LoginRequest;
import com.employeerating.dto.LoginResponse;


public interface AuthService {

  
    public LoginResponse login(LoginRequest request);
        
}
