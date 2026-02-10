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

            }

    @Bean
    public ModelMapper getMapper() {
        return new ModelMapper();
    }
}
