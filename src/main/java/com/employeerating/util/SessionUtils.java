package com.employeerating.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SessionUtils {

    public static String getEmployeeId() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return (String) attr.getRequest().getSession(false).getAttribute("employeeId");
    }

    public static void setEmployeeId(String employeeId) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        attr.getRequest().getSession(true).setAttribute("employeeId", employeeId);
    }
}