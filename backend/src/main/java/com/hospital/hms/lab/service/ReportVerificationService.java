package com.hospital.hms.lab.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.lab.dto.LabReportResponseDto;
import com.hospital.hms.lab.entity.LabAuditEventType;
import com.hospital.hms.lab.entity.LabReport;
import com.hospital.hms.lab.entity.ReportStatus;
import com.hospital.hms.lab.entity.TATStatus;
import com.hospital.hms.lab.entity.TestOrder;
import com.hospital.hms.lab.entity.TestStatus;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.lab.repository.LabReportRepository;
import com.hospital.hms.lab.repository.TestOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service for report verification, supervisor authorization, correction logging, and release workflow.
 */
@Service
public class ReportVerificationService {

    private static final Logger log = LoggerFactory.getLogger(ReportVerificationService.class);

    private final LabReportRepository labReportRepository;
    private final TestOrderRepository testOrderRepository;
    private final LabAuditService labAuditService;
    private final AtomicLong reportSequence = new AtomicLong(0);
    private volatile int lastYear = Year.now().getValue();

    public ReportVerificationService(LabReportRepository labReportRepository, TestOrderRepository testOrderRepository,
                                    LabAuditService labAuditService) {
        this.labReportRepository = labReportRepository;
        this.testOrderRepository = testOrderRepository;
        this.labAuditService = labAuditService;
    }

    /**
     * Generate report draft from completed test order.
     */
    @Transactional
    public LabReportResponseDto generateReport(Long testOrderId, String generatedBy) {
        TestOrder order = testOrderRepository.findById(testOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found: " + testOrderId));

        if (order.getStatus() != TestStatus.COMPLETED) {
            throw new IllegalArgumentException("Report can only be generated for completed tests. Current status: " + order.getStatus());
        }

        // Check if report already exists
        LabReport existingReport = labReportRepository.findByTestOrderId(testOrderId).orElse(null);
        if (existingReport != null) {
            return toDto(existingReport);
        }

        LabReport report = new LabReport();
        report.setReportNumber(generateReportNumber());
        report.setTestOrder(order);
        report.setStatus(ReportStatus.DRAFT);
        report.setGeneratedAt(LocalDateTime.now());
        report.setGeneratedBy(generatedBy);
        report.setReportContent("Report generated for " + order.getTestMaster().getTestName());

        report = labReportRepository.save(report);
        return toDto(report);
    }

    /**
     * Verify report (supervisor authorization). Technician cannot authorize own report.
     */
    @Transactional
    public LabReportResponseDto verifyReport(Long reportId, String verifiedBy, String supervisorSignature) {
        LabReport report = labReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));

        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new IllegalArgumentException("Report can only be verified when in DRAFT status. Current status: " + report.getStatus());
        }

        // Check: Technician cannot authorize own report
        if (verifiedBy.equals(report.getGeneratedBy())) {
            throw new IllegalArgumentException("Technician cannot authorize their own report");
        }

        report.setStatus(ReportStatus.VERIFIED);
        LocalDateTime verifiedAt = LocalDateTime.now();
        report.setVerifiedAt(verifiedAt);
        report.setVerifiedBy(verifiedBy);
        report.setSupervisorSignature(supervisorSignature);

        report = labReportRepository.save(report);

        // Update TestOrder: TAT = Result Verified Time - Sample Collection Time
        TestOrder order = report.getTestOrder();
        order.setStatus(TestStatus.VERIFIED);
        order.setVerifiedAt(verifiedAt);
        order.setVerifiedBy(verifiedBy);
        order.setTatEndTime(verifiedAt.atZone(java.time.ZoneId.systemDefault()).toInstant());
        evaluateTAT(order);
        testOrderRepository.save(order);

        labAuditService.log(LabAuditEventType.RESULT_VERIFIED, order.getId(), null, report.getId(), verifiedBy,
                order.getOrderNumber());

        if (log.isInfoEnabled()) {
            MDC.put(MdcKeys.MODULE, "LAB");
            log.info("LAB_AUDIT report_verification reportId={} orderNumber={} verifiedBy={} correlationId={}",
                    report.getId(), report.getTestOrder().getOrderNumber(), verifiedBy, MDC.get(MdcKeys.CORRELATION_ID));
            MDC.remove(MdcKeys.MODULE);
        }
        return toDto(report);
    }

    /**
     * Release report to doctor/patient. Makes report read-only.
     */
    @Transactional
    public LabReportResponseDto releaseReport(Long reportId, String releasedBy) {
        LabReport report = labReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));

        if (report.getStatus() != ReportStatus.VERIFIED) {
            throw new IllegalArgumentException("Report can only be released when VERIFIED. Current status: " + report.getStatus());
        }

        report.setStatus(ReportStatus.RELEASED);
        report.setReleasedAt(LocalDateTime.now());
        report.setReleasedBy(releasedBy);
        report.setIsReadOnly(true);

        // Update test order status
        TestOrder order = report.getTestOrder();
        order.setStatus(TestStatus.RELEASED);
        order.setReleasedAt(LocalDateTime.now());
        order.setReleasedBy(releasedBy);
        testOrderRepository.save(order);

        report = labReportRepository.save(report);
        if (log.isInfoEnabled()) {
            MDC.put(MdcKeys.MODULE, "LAB");
            log.info("LAB_AUDIT report_release reportId={} orderNumber={} releasedBy={} correlationId={}",
                    report.getId(), report.getTestOrder().getOrderNumber(), releasedBy, MDC.get(MdcKeys.CORRELATION_ID));
            MDC.remove(MdcKeys.MODULE);
        }
        return toDto(report);
    }

    /**
     * Search reports by UHID, patient name, test name, and date range.
     */
    @Transactional(readOnly = true)
    public List<LabReportResponseDto> searchReports(
            String uhid, String patientName, String testName,
            LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime to = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
        List<LabReport> reports = labReportRepository.searchReports(uhid, patientName, testName, from, to);
        return reports.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LabReportResponseDto findByTestOrderId(Long testOrderId) {
        LabReport report = labReportRepository.findByTestOrderId(testOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found for test order: " + testOrderId));
        return toDto(report);
    }

    /** TAT = Result Verified Time - Sample Collection Time. Mark BREACH if exceeded. */
    private void evaluateTAT(TestOrder order) {
        Instant start = order.getTatStartTime();
        if (start == null && order.getSampleCollectedAt() != null) {
            start = order.getSampleCollectedAt().atZone(java.time.ZoneId.systemDefault()).toInstant();
        }
        Instant end = order.getTatEndTime();
        if (start == null || end == null || order.getTestMaster().getNormalTATMinutes() == null) return;
        long actualMinutes = Duration.between(start, end).toMinutes();
        int normalMinutes = order.getTestMaster().getNormalTATMinutes();
        if (actualMinutes > normalMinutes) {
            order.setTatStatus(TATStatus.BREACH);
            if (order.getTatBreachReason() == null || order.getTatBreachReason().isEmpty()) {
                order.setTatBreachReason("TAT exceeded by " + (actualMinutes - normalMinutes) + " minutes");
            }
        } else {
            order.setTatStatus(TATStatus.WITHIN_TAT);
        }
    }

    private String generateReportNumber() {
        int currentYear = Year.now().getValue();
        if (currentYear != lastYear) {
            synchronized (this) {
                if (currentYear != lastYear) {
                    lastYear = currentYear;
                    reportSequence.set(0);
                }
            }
        }
        long seq = reportSequence.incrementAndGet();
        return String.format("RPT-%d-%06d", currentYear, seq);
    }

    private LabReportResponseDto toDto(LabReport r) {
        LabReportResponseDto dto = new LabReportResponseDto();
        dto.setId(r.getId());
        dto.setReportNumber(r.getReportNumber());
        dto.setTestOrderId(r.getTestOrder().getId());
        dto.setOrderNumber(r.getTestOrder().getOrderNumber());
        dto.setPatientName(r.getTestOrder().getPatient().getFullName());
        dto.setPatientUhid(r.getTestOrder().getPatient().getUhid());
        dto.setTestName(r.getTestOrder().getTestMaster().getTestName());
        dto.setStatus(r.getStatus());
        dto.setGeneratedAt(r.getGeneratedAt());
        dto.setGeneratedBy(r.getGeneratedBy());
        dto.setVerifiedAt(r.getVerifiedAt());
        dto.setVerifiedBy(r.getVerifiedBy());
        dto.setReleasedAt(r.getReleasedAt());
        dto.setReleasedBy(r.getReleasedBy());
        dto.setReportContent(r.getReportContent());
        dto.setInterpretation(r.getInterpretation());
        dto.setSupervisorSignature(r.getSupervisorSignature());
        dto.setCorrectionLog(r.getCorrectionLog());
        dto.setIsReadOnly(r.getIsReadOnly());
        dto.setPdfPath(r.getPdfPath());
        return dto;
    }
}
