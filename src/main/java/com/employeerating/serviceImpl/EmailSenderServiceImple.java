package com.employeerating.serviceImpl;

import com.employeerating.entity.Employee;
import com.employeerating.entity.Rating;
import com.employeerating.model.FileAttachmentModel;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.service.EmailSenderService;
import com.employeerating.util.ExcelGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmailSenderServiceImple implements EmailSenderService {

    private static final String FROM_EMAIL = "professional.rumango@gmail.com";

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmployeeRepo employeeRepo;

    // ================= CORE MAIL UTILITY =================

    private void sendMail(
            String to,
            String subject,
            String body,
            byte[] attachment,
            String attachmentName,
            String contentType
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            if (attachment != null && attachment.length > 0) {
                helper.addAttachment(
                        attachmentName,
                        new ByteArrayDataSource(attachment, contentType)
                );
            }

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }

    // ================= SIMPLE EMAIL =================

    @Override
    public void sendEmail(FileAttachmentModel model) {
        sendMail(
                model.getToEmail(),
                model.getSubject(),
                model.getBody(),
                null,
                null,
                null
        );
    }

    // ================= GENERIC ATTACHMENT =================

    public void sendEmailWithAttachment(FileAttachmentModel model) {
        sendMail(
                model.getToEmail(),
                model.getSubject(),
                model.getBody(),
                model.getAttachments(),
                "Employee_Ratings_" + LocalDate.now() + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
    }

    // ================= TL EMAIL =================

    @Override
    public void sendEmailWithAttachmentToTl(FileAttachmentModel model) {
        sendMail(
                model.getToEmail(),
                model.getSubject(),
                model.getBody(),
                model.getAttachments(),
                "EmployeeRatings.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
    }

    @Override
    public void sendEmailWithAttachementToPm(FileAttachmentModel model) {

    }

    // ================= PER-TL EXCEL =================

    @Override
    public void sendEmailWithAttachmentToPerTl(List<Rating> ratings) throws IOException {

        Map<String, byte[]> excelPerTL =
                ExcelGenerator.generateExcelPerTeamLead(
                        ratings.stream()
                                .filter(r -> r.getTeamLeadEmail() != null && !r.getTeamLeadEmail().isBlank())
                                .collect(Collectors.toList())
                );

        excelPerTL.forEach((tlEmail, excelBytes) -> {
            sendMail(
                    tlEmail,
                    "Employee Ratings Report",
                    """
                    Dear Team Lead,<br><br>
                    Please find attached the latest employee ratings report.<br><br>
                    Regards
                    """,
                    excelBytes,
                    "Employee_Ratings_" + tlEmail + ".xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
        });
    }

    // ================= PM EMAIL =================

    @Override
    public void sendEmailWithAttachmentToPm(FileAttachmentModel model) {
        sendMail(
                model.getToEmail(),
                model.getSubject(),
                model.getBody(),
                model.getAttachments(),
                "Rating.pdf",
                "application/pdf"
        );
    }

    @Override
    public void sendEmailWithAttachmentToPmo(FileAttachmentModel model) {

    }

    @Override
    public void sendEmailWithAttachmentToHr(FileAttachmentModel model) {

    }

    // ================= MONTHLY REPORT =================

    @Override
    public void sendMonthlyExcelReportToAllEmployees() {

        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());

        employeeRepo.findAll().stream()
                .filter(e -> "DEVELOPER".equalsIgnoreCase(e.getEmployeeRole()))
                .forEach(employee -> {

                    byte[] excel =
                            null;
                    try {
                        excel = ExcelGenerator.generateExcelForEmployee(employee, start, end);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (excel == null || excel.length == 0) return;

                    sendMail(
                            employee.getEmployeeEmail(),
                            "Monthly Performance Report",
                            buildMonthlyEmailBody(employee, start, end),
                            excel,
                            "Monthly_Report_" + employee.getEmployeeId() + ".xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    );
                });
    }

    // ================= WEEKLY REPORT =================

    @Override
    public void sendWeeklyExcelReportToAllEmployees() {

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        employeeRepo.findAll().stream()
                .filter(e -> "DEVELOPER".equalsIgnoreCase(e.getEmployeeRole()))
                .forEach(employee -> {

                    byte[] excel =
                            null;
                    try {
                        excel = ExcelGenerator.generateExcelForEmployee(employee, start, end);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (excel == null || excel.length == 0) return;

                    sendMail(
                            employee.getEmployeeEmail(),
                            "Weekly Performance Report",
                            buildWeeklyEmailBody(employee, start, end),
                            excel,
                            "Weekly_Report_" + employee.getEmployeeId() + ".xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    );
                });
    }

    // ================= EMAIL BODIES =================

    private String buildMonthlyEmailBody(Employee e, LocalDate s, LocalDate e1) {
        return """
        <p>Dear <b>%s</b>,</p>
        <p>Your <b>Monthly Performance Report</b> for %s to %s is attached.</p>
        <p>Regards,<br><b>Employee Rating System</b></p>
        """.formatted(e.getEmployeeName(), s, e1);
    }

    private String buildWeeklyEmailBody(Employee e, LocalDate s, LocalDate e1) {
        return """
        <p>Dear <b>%s</b>,</p>
        <p>Your <b>Weekly Performance Report</b> for %s to %s is attached.</p>
        <p>Regards,<br><b>Employee Rating System</b></p>
        """.formatted(e.getEmployeeName(), s, e1);
    }
}
