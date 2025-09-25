package com.employeerating;

import com.employeerating.entity.Employee;
import com.employeerating.entity.EmployeeTask;
import com.employeerating.entity.Rating;
import com.employeerating.util.ExcelGenerator;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@EnableScheduling
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class}, scanBasePackages = "com.employeerating")
public class EmployeeRatingApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(EmployeeRatingApplication.class);
    }

    public static void main(String[] args) throws IOException {
        SpringApplication.run(EmployeeRatingApplication.class, args);


//        List<Employee> employees = new ArrayList<>();
//
//        // create some dummy data
//        Employee e1 = new Employee();
//        e1.setEmployeeId("EMP001");
//        e1.setEmployeeName("John Doe");
//        e1.setEmployeeTasks(List.of(
////                new EmployeeTask("Task1", LocalDate.now()),
////                new EmployeeTask("Task2", LocalDate.now().minusDays(1))
//        ));
//        e1.setRatings(List.of(
//                new Rating(LocalDate.now(), 4),
//                new Rating(LocalDate.now().minusDays(1), 5)
//        ));
//
//        employees.add(e1);
//
//        byte[] excelData = ExcelGenerator.generateExcelForEmployees(employees);
//
//        // write to file
//        try (FileOutputStream fos = new FileOutputStream("employees.xlsx")) {
//            fos.write(excelData);
//        }
//
//        System.out.println("Excel generated: employees.xlsx");
    }

    @Bean
    public ModelMapper getMapper() {
        return new ModelMapper();
    }
}
