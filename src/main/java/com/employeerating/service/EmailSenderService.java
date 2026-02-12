package com.employeerating.service;

import com.employeerating.entity.Rating;
import org.springframework.stereotype.Service;

import com.employeerating.model.FileAttachmentModel;

import java.io.IOException;
import java.util.List;

@Service
public interface EmailSenderService {
    // my impl
    void sendEmailWithAttachment(FileAttachmentModel model);

    void sendEmail(FileAttachmentModel model);

    void sendEmailWithAttachmentToTl(FileAttachmentModel model);

    void sendEmailWithAttachementToPm(FileAttachmentModel model);

    void sendEmailWithAttachmentToPm(FileAttachmentModel model);

    void sendEmailWithAttachmentToPmo(FileAttachmentModel model);

    void sendEmailWithAttachmentToHr(FileAttachmentModel model);

    void sendEmailWithAttachmentToPerTl(List<Rating> employees) throws IOException;

	void sendMonthlyExcelReportToAllEmployees();

	void sendWeeklyExcelReportToAllEmployees();
}