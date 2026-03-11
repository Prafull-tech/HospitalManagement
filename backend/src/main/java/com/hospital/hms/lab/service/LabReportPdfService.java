package com.hospital.hms.lab.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.lab.entity.LabAuditEventType;
import com.hospital.hms.lab.entity.LabReport;
import com.hospital.hms.lab.entity.LabResult;
import com.hospital.hms.lab.entity.TestOrder;
import com.hospital.hms.lab.repository.LabReportRepository;
import com.hospital.hms.lab.repository.LabResultRepository;
import com.hospital.hms.lab.repository.TestOrderRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates PDF lab reports with hospital header, patient details, doctor name,
 * test results, reference ranges, and pathologist signature.
 */
@Service
public class LabReportPdfService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private final TestOrderRepository testOrderRepository;
    private final LabResultRepository labResultRepository;
    private final LabReportRepository labReportRepository;
    private final LabAuditService labAuditService;

    @Value("${lab.report.hospital-name:Sample Hospital}")
    private String hospitalName;

    @Value("${lab.report.hospital-address:123 Hospital Road, City}")
    private String hospitalAddress;

    @Value("${lab.report.hospital-phone:}")
    private String hospitalPhone;

    @Value("${lab.report.pathologist-name:Pathologist}")
    private String pathologistName;

    public LabReportPdfService(TestOrderRepository testOrderRepository,
                               LabResultRepository labResultRepository,
                               LabReportRepository labReportRepository,
                               LabAuditService labAuditService) {
        this.testOrderRepository = testOrderRepository;
        this.labResultRepository = labResultRepository;
        this.labReportRepository = labReportRepository;
        this.labAuditService = labAuditService;
    }

    /**
     * Generate PDF report. When printedBy is not null, logs REPORT_PRINTED audit event.
     */
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long testOrderId, String printedBy) throws DocumentException {
        TestOrder order = testOrderRepository.findById(testOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found: " + testOrderId));

        List<LabResult> results = labResultRepository.findByTestOrder_IdOrderByParameterNameAsc(testOrderId);
        LabReport report = labReportRepository.findByTestOrderId(testOrderId).orElse(null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, baos);
        document.open();

        // Hospital header
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph hospitalTitle = new Paragraph(hospitalName, headerFont);
        hospitalTitle.setAlignment(Element.ALIGN_CENTER);
        hospitalTitle.setSpacingAfter(4);
        document.add(hospitalTitle);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph address = new Paragraph(hospitalAddress + (hospitalPhone != null && !hospitalPhone.isBlank() ? " | " + hospitalPhone : ""), subFont);
        address.setAlignment(Element.ALIGN_CENTER);
        address.setSpacingAfter(2);
        document.add(address);

        Paragraph labDept = new Paragraph("Laboratory Report", subFont);
        labDept.setAlignment(Element.ALIGN_CENTER);
        labDept.setSpacingAfter(16);
        document.add(labDept);

        // Report number if available
        if (report != null && report.getReportNumber() != null) {
            Paragraph reportNo = new Paragraph("Report #: " + report.getReportNumber(), subFont);
            reportNo.setSpacingAfter(8);
            document.add(reportNo);
        }

        // Patient details
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        document.add(new Paragraph("Patient Details", labelFont));
        document.add(new Paragraph("Patient Name: " + (order.getPatient().getFullName() != null ? order.getPatient().getFullName() : "—")));
        document.add(new Paragraph("UHID: " + (order.getPatient().getUhid() != null ? order.getPatient().getUhid() : "—")));
        document.add(new Paragraph("Order #: " + (order.getOrderNumber() != null ? order.getOrderNumber() : "—")));
        document.add(new Paragraph("Ordered At: " + (order.getOrderedAt() != null ? order.getOrderedAt().format(DT_FMT) : "—")));
        document.add(new Paragraph(" "));

        // Doctor name
        document.add(new Paragraph("Referred By (Doctor): " + (order.getDoctor().getFullName() != null ? order.getDoctor().getFullName() : "—")));
        document.add(new Paragraph("Test: " + (order.getTestMaster().getTestName() != null ? order.getTestMaster().getTestName() : "—")));
        document.add(new Paragraph(" "));

        // Test results table
        if (!results.isEmpty()) {
            document.add(new Paragraph("Test Results", labelFont));
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(8);
            table.setSpacingAfter(16);

            table.addCell(createHeaderCell("Parameter"));
            table.addCell(createHeaderCell("Result"));
            table.addCell(createHeaderCell("Unit"));
            table.addCell(createHeaderCell("Reference Range"));
            table.addCell(createHeaderCell("Flag"));

            for (LabResult r : results) {
                table.addCell(createCell(r.getParameterName() != null ? r.getParameterName() : "Result"));
                table.addCell(createCell(r.getResultValue() != null ? r.getResultValue() : "—"));
                table.addCell(createCell(r.getUnit() != null ? r.getUnit() : "—"));
                table.addCell(createCell(r.getNormalRange() != null ? r.getNormalRange() : "—"));
                table.addCell(createCell(r.getFlag() != null ? r.getFlag() : "—"));
            }
            document.add(table);
        } else {
            document.add(new Paragraph("No results entered.", subFont));
            document.add(new Paragraph(" "));
        }

        // Pathologist signature
        String signature = pathologistName;
        if (report != null && report.getSupervisorSignature() != null && !report.getSupervisorSignature().isBlank()) {
            signature = report.getSupervisorSignature();
        } else if (report != null && report.getVerifiedBy() != null) {
            signature = report.getVerifiedBy();
        }
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Authorized by:", labelFont));
        document.add(new Paragraph(signature, FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));
        if (report != null && report.getVerifiedAt() != null) {
            document.add(new Paragraph("Verified: " + report.getVerifiedAt().format(DT_FMT), subFont));
        }

        document.close();
        if (printedBy != null && !printedBy.isBlank()) {
            labAuditService.log(LabAuditEventType.REPORT_PRINTED, testOrderId, null, null, printedBy,
                    order.getOrderNumber());
        }
        return baos.toByteArray();
    }

    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
        cell.setBackgroundColor(new java.awt.Color(0.9f, 0.9f, 0.9f));
        return cell;
    }

    private PdfPCell createCell(String text) {
        return new PdfPCell(new Phrase(text != null ? text : "—", FontFactory.getFont(FontFactory.HELVETICA, 9)));
    }
}
