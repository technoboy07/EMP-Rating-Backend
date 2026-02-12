package com.employeerating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String message;
    private String role;
    private String employeeId;  // returning employeeId (userId from DB)
    private String employeeName;
    private String redirectUrl;
    private String email;
}
