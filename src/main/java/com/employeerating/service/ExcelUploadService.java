package com.employeerating.service;

import org.springframework.web.multipart.MultipartFile;

public interface ExcelUploadService {
	
	public void uploadExcel(MultipartFile file) throws Exception;

    void uploadEmployeeDetailsExcel(MultipartFile file) throws Exception;

}
