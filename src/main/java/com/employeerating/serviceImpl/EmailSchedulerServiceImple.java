package com.employeerating.serviceImpl;

import com.employeerating.entity.Employee;
import com.employeerating.entity.Rating;
import com.employeerating.model.FileAttachmentModel;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.repository.RatingRepo;
import com.employeerating.service.EmailSchedulerService;
import com.employeerating.service.EmailSenderService;
import com.employeerating.util.ExcelGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmailSchedulerServiceImple implements EmailSchedulerService {

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RatingRepo ratingRepo;

    // ================= BASIC TL REMINDER =================

    @Override
    public void sendEmailParticular() {
        try {
            List<String> teamLeadEmails = employeeRepo.findAll().stream()
                    .map(Employee::getTeamLeadEmail)
                    .filter(email -> email != null && !email.isBlank())
                    .distinct()
                    .collect(Collectors.toList());

            for (String teamLeadEmail : teamLeadEmails) {
                FileAttachmentModel model = new FileAttachmentModel();
                model.setToEmail(teamLeadEmail);
                model.setSubject("Rate your employees");

                emailSenderService.sendEmailWithAttachmentToTl(model);
                log.info("Rating reminder sent to TL {}", teamLeadEmail);
            }

        } catch (Exception e) {
            log.error("Failed to send TL rating reminders", e);
        }
    }

    // ================= MONTHLY TL SUMMARY =================

    @Transactional
    @Scheduled(cron = "0 0 18 L * ?", zone = "Asia/Kolkata")
    public void sendEmailToTl() {

        try {
            LocalDate now = LocalDate.now();
            LocalDate startDate = now.withDayOfMonth(1);
            LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

            List<Rating> allRatings = ratingRepo.findAll().stream()
                    .filter(r -> r.getRatingDate() != null
                            && !r.getRatingDate().isBefore(startDate)
                            && !r.getRatingDate().isAfter(endDate))
                    .collect(Collectors.toList());

            Map<String, byte[]> excelPerTL =
                    ExcelGenerator.generateExcelPerTeamLead(allRatings);

            for (Map.Entry<String, byte[]> entry : excelPerTL.entrySet()) {
                String teamLeadEmail = entry.getKey();

                try {
                    FileAttachmentModel model = new FileAttachmentModel();
                    model.setToEmail(teamLeadEmail);
                    model.setSubject("Employee Rating Summary (" + startDate + " to " + endDate + ")");
                    model.setBody("""
                            <p>Dear Team Lead,</p>
                            <p>Please find attached the rating summary for employees under your supervision for this month.</p>
                            <p>If already submitted, kindly ignore.</p>
                            <br>
                            <p>Regards,<br>
                            Rumango Software and Consulting Services Pvt. Ltd.</p>
                            """);
                    model.setAttachments(entry.getValue());

                    emailSenderService.sendEmailWithAttachmentToTl(model);
                    log.info("Monthly TL report sent to {}", teamLeadEmail);

                } catch (Exception e) {
                    log.error("Failed to send monthly TL report to {}", teamLeadEmail, e);
                }
            }

        } catch (Exception e) {
            log.error("Failed while processing monthly TL emails", e);
        }
    }

    // ================= MONTHLY PM / DELIVERY HEAD =================

    @Transactional
    @Scheduled(cron = "0 0 18 L * ?", zone = "Asia/Kolkata")
    public void sendEmailToPm() {

        List<String> targetEmails = employeeRepo.findAll().stream()
                .filter(emp ->
                        "PMO".equalsIgnoreCase(emp.getEmployeeRole()) ||
                                "Delivery Head".equalsIgnoreCase(emp.getEmployeeRole()))
                .map(Employee::getEmployeeEmail)
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .collect(Collectors.toList());

        List<Employee> employees = employeeRepo.findAll().stream()
                .filter(emp -> "Developer".equalsIgnoreCase(emp.getEmployeeRole()))
                .collect(Collectors.toList());

        // Initialize lazy collections
        employees.forEach(emp -> {
            if (emp.getRatings() != null) emp.getRatings().size();
            if (emp.getEmployeeTasks() != null) emp.getEmployeeTasks().size();
        });

        for (String pmEmail : targetEmails) {
            try {
                byte[] excelBytes =
                        ExcelGenerator.generateExcelForEmployeesPM(employees);

                FileAttachmentModel model = new FileAttachmentModel();
                model.setToEmail(pmEmail);
                model.setSubject("Employee Ratings Summary");
                model.setBody("""
                        <p>Dear Colleague,</p>
                        <p>This is a gentle reminder to confirm the employee ratings for the current month.
                           If the ratings have already been submitted, kindly disregard this email.</p>
                        <br>
                        <p>Best Regards,<br>
                        Rumango Software and Consulting Services Pvt. Ltd.</p>
                        """);
                model.setAttachments(excelBytes);

                emailSenderService.sendEmailWithAttachment(model);
                log.info("PM summary email sent to {}", pmEmail);

            } catch (Exception e) {
                log.error("Failed to send PM summary email to {}", pmEmail, e);
            }
        }
    }

    // ================= CLEANUP OLD RATINGS =================

    public List<String> deletePreviousRatings() {

        LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2);
        List<Rating> oldRatings = ratingRepo.findRatingsBeforeDate(twoMonthsAgo);

        List<String> employees = oldRatings.stream()
                .filter(r -> r.getEmployee() != null)
                .map(r -> r.getEmployee().getEmployeeName())
                .collect(Collectors.toList());

        log.info("Found {} employees with ratings older than {}", employees.size(), twoMonthsAgo);
        return employees;
    }

    @Override
    public void sendEmailToPmo() {
        log.info("sendEmailToPmo called – not implemented");
    }

    @Override
    public void sendEmailToHr() {
        log.info("sendEmailToHr called – not implemented");
    }

    // ================= EMPLOYEE REPORTS =================

    // 26th & last day of month
    @Scheduled(cron = "0 0 18 26,L * ?", zone = "Asia/Kolkata")
    public void sendMonthlyReports() {
        log.info("Starting monthly employee reports job");
        emailSenderService.sendMonthlyExcelReportToAllEmployees();
    }

    // Every Friday 6 PM IST
    @Scheduled(cron = "0 0 18 ? * FRI", zone = "Asia/Kolkata")
    public void sendWeeklyReports() {
        log.info("Starting weekly employee reports job");
        emailSenderService.sendWeeklyExcelReportToAllEmployees();
    }
}
