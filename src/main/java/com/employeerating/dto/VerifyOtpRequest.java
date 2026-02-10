package com.employeerating.dto;

import lombok.Data;

@Data
public class VerifyOtpRequest {
	
	private String employeeId;
	private String otp;

}
