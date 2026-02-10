package com.employeerating.util;

import com.employeerating.entity.Employee;
import com.employeerating.entity.Rating;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
public class PdfGenerator {

    private PdfGenerator() {
        // utility class – prevent instantiation
    }

    public static byte[] generatePdf(Employee employee) {

        if (employee == null) {
            log.error("Employee is null while generating PDF");
            throw new IllegalArgumentException("Employee cannot be null");
        }

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // ===== Title =====
            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title =
                    new Paragraph("Employee Details - " + employee.getEmployeeId(), font);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(Chunk.NEWLINE);

            // ===== Ratings Table =====
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            table.addCell("Daily Rating");
            table.addCell("Rating Date");
            table.addCell("Rated By");
            table.addCell("TL Submit Date");
            table.addCell("PM Submit Date");

            List<Rating> ratings = employee.getRatings();

            if (ratings != null && !ratings.isEmpty()) {
                for (Rating rating : ratings) {
                    table.addCell(
                            rating.getDailyRating() != null
                                    ? rating.getDailyRating().toString()
                                    : "N/A"
                    );
                    table.addCell(
                            rating.getRatingDate() != null
                                    ? rating.getRatingDate().toString()
                                    : "N/A"
                    );
                    table.addCell(
                            rating.getRatedBy() != null
                                    ? rating.getRatedBy()
                                    : "N/A"
                    );
                    table.addCell(
                            rating.getTlSubmitDate() != null
                                    ? rating.getTlSubmitDate().toString()
                                    : "N/A"
                    );
                    table.addCell(
                            rating.getPmSubmitDate() != null
                                    ? rating.getPmSubmitDate().toString()
                                    : "N/A"
                    );
                }
            } else {
                PdfPCell cell = new PdfPCell(new Phrase("No ratings found"));
                cell.setColspan(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            document.add(table);
            document.close();

            log.info(
                    "PDF generated successfully for employeeId={}",
                    employee.getEmployeeId()
            );

            return out.toByteArray();

        } catch (Exception e) {
            log.error(
                    "Error generating PDF for employeeId={}",
                    employee.getEmployeeId(),
                    e
            );
            throw new RuntimeException("Failed to generate employee PDF", e);
        }
    }
}
