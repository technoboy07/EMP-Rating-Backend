package com.employeerating.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.employeerating.service.ExcelUploadService;

@RestController
@RequestMapping("/excel")
public class ExcelUploadController {

    @Autowired
    private ExcelUploadService excelUploadService;

    // POST endpoint to upload Excel file
    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Please select an Excel file to upload.");
            }

            excelUploadService.uploadExcel(file);
            return ResponseEntity.ok("✅ Excel uploaded and users saved to DB successfully.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Failed to upload Excel: " + e.getMessage());
        }
    }


    // New Excel (employee details)
    @PostMapping("/uploadEmployeeDetails")
    public ResponseEntity<String> uploadEmployeeDetails(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Please select an Excel file to upload.");
            }

            excelUploadService.uploadEmployeeDetailsExcel(file);
            return ResponseEntity.ok("✅ Employee details Excel uploaded successfully.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Failed to upload Employee details Excel: " + e.getMessage());
        }
    }
}

