package com.employeerating.util;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.employeerating.entity.Employee;
import com.employeerating.entity.Rating;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PdfGenerator {
//change by me
    public static byte[] generatePdf(Employee employee) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Employee Details - " + employee.getEmployeeId(), font);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" ")); // spacing

            // Table for ratings
            PdfPTable table = new PdfPTable(5); // 5 columns
            table.setWidthPercentage(100);

            // Table headers
            table.addCell("Daily Rating");
            table.addCell("Rating Date");
            table.addCell("Rated By");
            table.addCell("TL Submit Date");
            table.addCell("PM Submit Date");

            // Get all ratings for the employee
            List<Rating> ratings = employee.getRatings();
            if (ratings != null && !ratings.isEmpty()) {
                for (Rating rating : ratings) {
                    table.addCell(rating.getDailyRating() != null ? rating.getDailyRating().toString() : "N/A");
                    table.addCell(rating.getRatingDate() != null ? rating.getRatingDate().toString() : "N/A");
                    table.addCell(rating.getRatedBy() != null ? rating.getRatedBy() : "N/A");
                    table.addCell(rating.getTlSubmitDate() != null ? rating.getTlSubmitDate().toString() : "N/A");
                    table.addCell(rating.getPmSubmitDate() != null ? rating.getPmSubmitDate().toString() : "N/A");
                }
            } else {
                PdfPCell cell = new PdfPCell(new Phrase("No ratings found"));
                cell.setColspan(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
}
