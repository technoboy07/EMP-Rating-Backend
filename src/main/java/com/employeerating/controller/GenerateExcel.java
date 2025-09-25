package com.employeerating.controller;

import com.employeerating.entity.Employee;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.util.ExcelGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/excel")
public class GenerateExcel {

    @Autowired
    EmployeeRepo employeeRepo;

    @GetMapping("/generate")
    public String getListOfEmployee() throws IOException {

        List<Employee> employees = employeeRepo.findAll();
        ExcelGenerator.generateExcelForEmployees(employees);
        return "Success";
    }


}
