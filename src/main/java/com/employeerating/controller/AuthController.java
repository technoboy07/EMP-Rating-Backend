package com.employeerating.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employeerating.dto.ForgotPasswordRequest;
import com.employeerating.dto.LoginRequest;
import com.employeerating.dto.LoginResponse;
import com.employeerating.dto.ResetPasswordRequest;
import com.employeerating.dto.VerifyOtpRequest;
import com.employeerating.service.AuthService;
import com.employeerating.serviceImpl.OtpService;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private OtpService otpService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
    
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return "OTP sent to registered email";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestBody VerifyOtpRequest request) {
        boolean valid = otpService.verifyOtp(request);
        return valid ? "OTP verified" : "Invalid OTP";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody ResetPasswordRequest request) {
            authService.resetPassword(request);
            return "Password reset successful";

    }
}
