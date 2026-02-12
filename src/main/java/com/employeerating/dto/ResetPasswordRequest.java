package com.employeerating.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {

	private String employeeId;
	private String newPassword;
	private String conformPassword;
}
