package com.employeerating.serviceImpl;

import javax.activation.DataSource;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import com.employeerating.entity.Rating;
import com.employeerating.util.ExcelGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.employeerating.model.FileAttachmentModel;
import com.employeerating.service.EmailSenderService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class EmailSenderServiceImple implements EmailSenderService {

	@Autowired
	JavaMailSender mailSender;

//	@Override
//	public void sendEmail(FileAttachmentModel model) {
//		SimpleMailMessage message = new SimpleMailMessage();
//		message.setFrom("ranjitsutar.offc456@gmail.com");
//		message.setTo(model.getToEmail());
//		message.setSubject(model.getSubject());
//		message.setText(model.getBody());
//		mailSender.send(message);
//	}

    public void sendEmail(FileAttachmentModel model) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("anweshasathua0513@gmail.com");
            helper.setTo(model.getToEmail());
            helper.setSubject(model.getSubject());
            helper.setText(model.getBody(), true);

            mailSender.send(message);
            System.out.println("Email sent to " + model.getToEmail());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //my impl
    public void sendEmailWithAttachment(FileAttachmentModel model) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("ranjitsutar.offc456@gmail.com");
            helper.setTo(model.getToEmail());
            helper.setSubject(model.getSubject());
            helper.setText(model.getBody(), true);
       /*     try {
                ClassPathResource logo = new ClassPathResource("static/Logo.png");
                if (logo.exists()) {
                    helper.addInline("logoImage", logo);
                }
            } catch (Exception ex) {
                System.out.println("Logo not found, skipping embedding.");
            }*/

            if (model.getAttachments() != null) {
                String fileName = "Employee_Ratings_" + LocalDate.now() + ".xlsx";
                DataSource dataSource = new ByteArrayDataSource(model.getAttachments(),
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                helper.addAttachment(fileName, dataSource);
            }

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void sendEmailWithAttachmentToTl(FileAttachmentModel model) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("amareshparida20@gmail.com");
            helper.setTo(model.getToEmail());
            helper.setSubject(model.getSubject());
            helper.setText(model.getBody(), true);

//            ClassPathResource logo = new ClassPathResource("static/Logo.png");
//            helper.addInline("logoImage", logo);

            if (model.getAttachments() != null) {
                ByteArrayDataSource dataSource = new ByteArrayDataSource(
                        model.getAttachments(),
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                );
                helper.addAttachment("EmployeeRatings.xlsx", dataSource);
            }

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


// my implimentation
@Override
public void sendEmailWithAttachmentToPerTl(List<Rating> employees) {

    try {
        // Null-safe grouping and Excel generation
        Map<String, byte[]> excelPerTL = ExcelGenerator.generateExcelPerTeamLead(
                employees.stream()
                        .filter(e -> e.getTeamLeadEmail() != null && !e.getTeamLeadEmail().isEmpty())
                        .collect(Collectors.toList())
        );

        for (Map.Entry<String, byte[]> entry : excelPerTL.entrySet()) {
            String tlEmail = entry.getKey();
            byte[] excelBytes = entry.getValue();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

//            helper.setFrom("ranjitsutar.offc456@gmail.com");
            helper.setTo(tlEmail);
            helper.setSubject("Employee Ratings Report");
            helper.setText(
                    "Dear Team Lead,<br><br>Please find attached the latest employee ratings report.<br><br>Regards",
                    true
            );

            // Inline logo
//            ClassPathResource logo = new ClassPathResource("static/Logo.png");
//            helper.addInline("logoImage", logo);

            // Attach Excel
            if (excelBytes != null && excelBytes.length > 0) {
                String fileName = "Employee_Ratings_" + tlEmail + ".xlsx";
                ByteArrayDataSource dataSource = new ByteArrayDataSource(
                        excelBytes,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                );
                helper.addAttachment(fileName, dataSource);
            }

            mailSender.send(message);
            System.out.println("Email sent to TL: " + tlEmail);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}






	@Override
	public void sendEmailWithAttachementToPm(FileAttachmentModel model) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(model.getToEmail());
			helper.setSubject(model.getSubject());
			helper.setText(model.getBody(), true);

//			ClassPathResource logo = new ClassPathResource("static/Logo.png"); // or "static/logo.png"
//			helper.addInline("logoImage", logo); // ID must match cid:logoImage

			if (model.getAttachments() != null) {
				ByteArrayDataSource dataSource = new ByteArrayDataSource(model.getAttachments(), "application/pdf");
				helper.addAttachment("Rating.pdf", dataSource);
			}

			mailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void sendEmailWithAttachmentToPm(FileAttachmentModel model) {
		// PM email functionality not implemented - using existing sendEmailWithAttachementToPm method
		sendEmailWithAttachementToPm(model);
	}

	@Override
	public void sendEmailWithAttachmentToPmo(FileAttachmentModel model) {
		// PMO email functionality not implemented - commented out as requested
		// try {
		//     MimeMessage message = mailSender.createMimeMessage();
		//     MimeMessageHelper helper = new MimeMessageHelper(message, true);
		//     helper.setFrom("amareshparida20@gmail.com");
		//     helper.setTo(model.getToEmail());
		//     helper.setSubject(model.getSubject());
		//     helper.setText(model.getBody(), true);
		//     ClassPathResource logo = new ClassPathResource("static/Logo.png");
		//     helper.addInline("logoImage", logo);
		//     if (model.getAttachments() != null) {
		//         ByteArrayDataSource dataSource = new ByteArrayDataSource(model.getAttachments(), "application/pdf");
		//         helper.addAttachment("Rating.pdf", dataSource);
		//     }
		//     mailSender.send(message);
		// } catch (Exception e) {
		//     e.printStackTrace();
		// }
	}

	@Override
	public void sendEmailWithAttachmentToHr(FileAttachmentModel model) {
		// HR email functionality not implemented - commented out as requested
		// try {
		//     MimeMessage message = mailSender.createMimeMessage();
		//     MimeMessageHelper helper = new MimeMessageHelper(message, true);
		//     helper.setFrom("amareshparida20@gmail.com");
		//     helper.setTo(model.getToEmail());
		//     helper.setSubject(model.getSubject());
		//     helper.setText(model.getBody(), true);
		//     ClassPathResource logo = new ClassPathResource("static/Logo.png");
		//     helper.addInline("logoImage", logo);
		//     if (model.getAttachments() != null) {
		//         ByteArrayDataSource dataSource = new ByteArrayDataSource(model.getAttachments(), "application/pdf");
		//         helper.addAttachment("Rating.pdf", dataSource);
		//     }
		//     mailSender.send(message);
		// } catch (Exception e) {
		//     e.printStackTrace();
		// }
	}

}
