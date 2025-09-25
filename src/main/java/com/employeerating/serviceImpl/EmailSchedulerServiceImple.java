package com.employeerating.serviceImpl;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import com.employeerating.util.ExcelGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employeerating.entity.Employee;
import com.employeerating.entity.Rating;
import com.employeerating.model.FileAttachmentModel;
import com.employeerating.repository.EmployeeRepo;
import com.employeerating.repository.RatingRepo;
import com.employeerating.service.EmailSchedulerService;
import com.employeerating.service.EmailSenderService;

import static java.time.LocalTime.now;

@Service
public class EmailSchedulerServiceImple implements EmailSchedulerService {
    @Autowired
    EmailSenderService emailSenderService;
    @Autowired
    EmployeeRepo employeeRepo;
    @Autowired
    EntityManager entityManager;
    @Autowired
    RatingRepo ratingRepo;


    // @Scheduled(cron = "0 0 11 25 * ?") have to change it to 25th of every month
//    @Scheduled(cron = "0 */5 * * * ?")
    @Override
    public void sendEmailParticular() {
        try {
//			String htmlTemplate = getHtmlTemplate("email-template(rating page).html");
            // Collect all distinct team lead emails
            List<String> teamLeadEmails = employeeRepo.findAll().stream().map(Employee::getTeamLeadEmail).filter(email -> email != null && !email.isEmpty()).distinct().collect(Collectors.toList());

            for (String teamLeadEmail : teamLeadEmails) {
//				String htmlContent = htmlTemplate;
                // Replace CTA link with TL-specific link
//				htmlContent = htmlContent.replace("<a href=\"#\" class=\"btn\">Please Give the Employee Ratings</a>",
//						"<a href=\"https://employee-rating-six.vercel.app/employee?teamLeadEmail=" + teamLeadEmail + "\" class=\"btn\">Please Give the Employee Ratings</a>");
                // Generic salutation
//				htmlContent = htmlContent.replace("${name}", "TEAM LEAD");

                FileAttachmentModel model = new FileAttachmentModel();
                model.setToEmail(teamLeadEmail);
                model.setSubject("Rate your employees");
//				model.setBody(htmlContent);
                emailSenderService.sendEmailWithAttachmentToTl(model);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

// my impl

    //    @Scheduled(cron = "0 0 12 22 * ?") // run 22nd of every month at 12:00
//    @Scheduled(cron = "0 * * * * ?")
//    @Scheduled(cron = "0 0/2 * * * ?")
//    @Scheduled(cron = "0 0/3 * * * ?")

    @Transactional
    public void sendEmailToTl() {
        try {
            List<Rating> allRatings = ratingRepo.findAll();  //fetch from rating table
            Map<String, byte[]> excelPerTL = ExcelGenerator.generateExcelPerTeamLead(allRatings);

//        String htmlTemplate = getHtmlTemplate("email-template-pmo-hr.html");

            for (String teamLeadEmail : excelPerTL.keySet()) {
//            String htmlContent = htmlTemplate;

//            // Replace CTA with TL-specific download link
//            String downloadLink = "https://192.168.0.22:8243/employee/api/download-excel?tlEmail=" + teamLeadEmail;
////            htmlContent = htmlContent.replace(
////                    "<a href=\"#\" class=\"btn\">Download Excel</a>",
////                    "<a href=\"" + downloadLink + "\" class=\"btn\">Download Excel</a>"
////            );

                // Replace placeholder name
//            htmlContent = htmlContent.replace("${name}", "TEAM LEAD");

                FileAttachmentModel model = new FileAttachmentModel();
                model.setToEmail(teamLeadEmail);
                model.setSubject("Employee Ratings Summary");
                model.setBody("""
                        <p>Dear Colleague,</p>
                        <p>This is a gentle reminder to confirm the employee ratings for the current month in order to proceed with the closure of the process.</p>
                        <p>If the ratings have already been submitted, kindly disregard this email.</p>
                        <br>
                        <p>Thank you for your attention and support.</p>
                        <p>Best Regards,</p>
                        <p>Rumango Software and Consulting Services Pvt. Ltd.</p>
                    """);
                //            model.setBody(htmlContent);
                model.setAttachments(excelPerTL.get(teamLeadEmail));

                emailSenderService.sendEmailWithAttachmentToTl(model);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //    @Scheduled(cron = "0 0 12 22 * ?")
    @Transactional
//    @Scheduled(cron = "0 * * * * ?")
//    @Scheduled(cron = "0 0/2 * * * ?")
//    @Scheduled(cron = "0 0/3 * * * ?")

    //    @Scheduled(cron = "0 0 12 22 * ?")
    public void sendEmailToPm() {
        List<String> targetEmails = List.of("ranjitsutar456@gmail.com");
        List<Employee> employees = employeeRepo.findAll().stream()
                .filter(emp -> "Developer".equalsIgnoreCase(emp.getEmployeeRole()))
                .collect(Collectors.toList());

        //    List<String> targetEmails = employees.stream()
        //            .filter(emp -> "PMO".equalsIgnoreCase(emp.getEmployeeRole()))
        //            .map(Employee::getPmoEmail)
        //            .filter(email -> email != null && !email.isEmpty())
        //            .distinct() // removes duplicates
        //            .collect(Collectors.toList());

        employees.forEach(emp -> {
            if (emp.getRatings() != null) emp.getRatings().size();         // forces loading ratings
            if (emp.getEmployeeTasks() != null) emp.getEmployeeTasks().size(); // forces loading tasks
        });
        //    // initialize ratings to avoid LazyInitializationException
        //    employees.forEach(emp -> {
        //        if (emp.getRatings() != null) {
        //            emp.getRatings().size();
        //        }
        //    });

        for (String pmEmail : targetEmails) {
            try {
                //            String htmlContent = getHtmlTemplate("email-template-pmo-hr.html");

                // dynamic link per PM
//                           String ratingLink = "https://192.168.0.22:8243/employee/api/download-excel-mail?pmEmail=" + pmEmail;

                // replace placeholders
                //   htmlContent = htmlContent.replace("${ratinglink}", ratingLink);
                // htmlContent = htmlContent.replace("${name}", "PROJECT MANAGER");

                // generate Excel
                byte[] excelBytes = ExcelGenerator.generateExcelForEmployeesPM(employees);

                // build email model
                FileAttachmentModel model = new FileAttachmentModel();
                model.setToEmail(pmEmail);
                model.setSubject("Employee Ratings Summary");
                model.setBody("""
                        <p>Dear Colleague,</p>
                        <p>This is a gentle reminder to confirm the employee ratings for the current month in order to proceed with the closure of the process.
                           If the ratings have already been submitted, kindly disregard this email.</p>
                        <br>
                        <p>Thank you for your attention and support.</p>
                        <p>Best Regards,
                           Rumango Software and Consulting Services Pvt. Ltd.</p>
                    """);
//            model.setBody(htmlContent);
                model.setAttachments(excelBytes);

                // send email
                emailSenderService.sendEmailWithAttachment(model);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> deletePreviousRatings() {
        LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2);

        // Fetch all ratings before 2 months ago
        List<Rating> oldRatings = ratingRepo.findRatingsBeforeDate(twoMonthsAgo);

        // Collect employee names, skipping null employees
        List<String> employees = oldRatings.stream()
                .filter(r -> r.getEmployee() != null)
                .map(r -> r.getEmployee().getEmployeeName())
                .collect(Collectors.toList());

        // Optional: delete old ratings
        // ratingRepo.deleteAll(oldRatings);

        return employees;
    }

    // @Scheduled(cron = "0 0 13 27 * ?")
    @Override
    public void sendEmailToPmo() {
        // PMO email functionality not implemented - commented out as requested
        // FileAttachmentModel model = new FileAttachmentModel();
        // List<Employee> employees = employeeRepo.findAll();
        // LocalDate today = LocalDate.now();
        // // Group eligible employees by PMO email and send one email per PMO
        // Map<String, List<Employee>> employeesByPmo = employees.stream()
        // 		.filter(employee -> (employee.getRating() != null && employee.getRating().getPmSubmitDate() != null
        // 				&& employee.getRating().getSendDateToPmo() == null
        // 				&& !(employee.isNoticePeriod() || employee.isProbationaPeriod())))
        // 		.collect(Collectors.groupingBy(Employee::getPmoEmail));

        // for (Map.Entry<String, List<Employee>> entry : employeesByPmo.entrySet()) {
        // 	String pmoEmail = entry.getKey();
        // 	List<Employee> pmoEmployees = entry.getValue();
        // 	if (pmoEmployees == null || pmoEmployees.isEmpty())
        // 		continue;
        // 	try {
        // 		String htmlContent = getHtmlTemplate("email-template(rating page).html");
        // 		// Replace CTA link with PMO-specific link
        // 		htmlContent = htmlContent.replace("<a href=\"#\" class=\"btn\">Please Give the Employee Ratings</a>",
        // 				"<a href=\"https://employee-rating-six.vercel.app/pmo?pmoEmail=" + pmoEmail + "\" class=\"btn\">Please Give the Employee Ratings</a>");
        // 		// Personalize name with PMO name if available
        // 		String pmoName = pmoEmployees.get(0).getPmoName() != null ? pmoEmployees.get(0).getPmoName().toUpperCase() : "PMO";
        // 		htmlContent = htmlContent.replace("${name}", pmoName);

        // 		model.setToEmail(pmoEmail);
        // 		model.setSubject("Rate your employees");
        // 		model.setBody(htmlContent);
        // 		emailSenderService.sendEmailWithAttachmentToPmo(model);

        // 		// Mark send date for all employees under this PMO
        // 		for (Employee employee : pmoEmployees) {
        // 			Rating rating = employee.getRating();
        // 			if (rating != null) {
        // 				rating.setSendDateToPmo(LocalDate.now());
        // 				ratingRepo.save(rating);
        // 			}
        // 		}
        // 	} catch (Exception e) {
        // 		e.printStackTrace();
        // 	}
        // }
    }

    // @Scheduled(cron = "0 0 14 28 * ?")
    @Override
    public void sendEmailToHr() {
        // HR email functionality not implemented - commented out as requested
        // FileAttachmentModel model = new FileAttachmentModel();
        // List<Employee> employees = employeeRepo.findAll();
        // LocalDate today = LocalDate.now();
        // // Filter employees eligible for HR email
        // List<Employee> hrEligibleEmployees = employees.stream()
        // 		.filter(employee -> (employee.getRating() != null && employee.getRating().getPmoSubmitDate() != null
        // 				&& employee.getRating().getSendToHr() == null
        // 				&& !(employee.isNoticePeriod() || employee.isProbationaPeriod())))
        // 		.collect(Collectors.toList());

        // if (hrEligibleEmployees.isEmpty())
        // 	return;

        // try {
        // 	String htmlContent = getHtmlTemplate("email-template(rating page).html");
        // 	// Replace CTA link with HR-specific link
        // 	htmlContent = htmlContent.replace("<a href=\"#\" class=\"btn\">Please Give the Employee Ratings</a>",
        // 			"<a href=\"https://employee-rating-six.vercel.app/hr\" class=\"btn\">Please Give the Employee Ratings</a>");
        // 	htmlContent = htmlContent.replace("${name}", "HR");

        // 	model.setToEmail("hr@rumango.com"); // Replace with actual HR email
        // 	model.setSubject("Employee ratings ready for review");
        // 	model.setBody(htmlContent);
        // 	emailSenderService.sendEmailWithAttachmentToHr(model);

        // 	// Mark send date for all eligible employees
        // 	for (Employee employee : hrEligibleEmployees) {
        // 		Rating rating = employee.getRating();
        // 		if (rating != null) {
        // 			rating.setSendToHr(LocalDate.now());
        // 			ratingRepo.save(rating);
        // 		}
        // 	}
        // } catch (Exception e) {
        // 	e.printStackTrace();
        // }
    }


    //  For Testing Local

}
