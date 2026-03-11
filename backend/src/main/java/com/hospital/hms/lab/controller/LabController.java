package com.hospital.hms.lab.controller;

import com.hospital.hms.lab.dto.LabAuditLogResponseDto;
import com.hospital.hms.lab.dto.LabDashboardMetricsDto;
import com.hospital.hms.lab.dto.LabDashboardOverviewDto;
import com.hospital.hms.lab.dto.LabDashboardResponseDto;
import com.hospital.hms.lab.dto.LabDashboardSummaryDto;
import com.hospital.hms.lab.dto.LabOrderItemResponseDto;
import com.hospital.hms.lab.dto.LabOrderRequestDto;
import com.hospital.hms.lab.dto.LabOrderResponseDto;
import com.hospital.hms.lab.dto.LabReportResponseDto;
import com.hospital.hms.lab.dto.LabResultEntryRequestDto;
import com.hospital.hms.lab.dto.LabResultRequestDto;
import com.hospital.hms.lab.dto.LabResultResponseDto;
import com.hospital.hms.lab.dto.LabTodaySummaryDto;
import com.hospital.hms.lab.dto.SampleCollectionRequestDto;
import com.hospital.hms.lab.dto.TestMasterRequestDto;
import com.hospital.hms.lab.dto.TestMasterResponseDto;
import com.hospital.hms.ipd.dto.DischargePendingItemDto;
import com.hospital.hms.lab.dto.TestOrderRequestDto;
import com.hospital.hms.lab.dto.TestOrderResponseDto;
import com.hospital.hms.lab.config.LabRoleAccess;
import com.hospital.hms.lab.entity.LabAuditLog;
import com.hospital.hms.lab.entity.TestStatus;
import com.hospital.hms.lab.service.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Laboratory Information System (LIS) operations.
 * Role-based access control enforced via @PreAuthorize annotations.
 */
@RestController
@RequestMapping("/lab")
public class LabController {

    private final TestMasterService testMasterService;
    private final TestOrderService testOrderService;
    private final SampleCollectionService sampleCollectionService;
    private final LabProcessingService labProcessingService;
    private final ReportVerificationService reportVerificationService;
    private final LabDashboardService labDashboardService;
    private final LabOrderService labOrderService;
    private final LabReportPdfService labReportPdfService;
    private final LabAuditService labAuditService;

    public LabController(
            TestMasterService testMasterService,
            TestOrderService testOrderService,
            SampleCollectionService sampleCollectionService,
            LabProcessingService labProcessingService,
            ReportVerificationService reportVerificationService,
            LabDashboardService labDashboardService,
            LabOrderService labOrderService,
            LabReportPdfService labReportPdfService,
            LabAuditService labAuditService) {
        this.testMasterService = testMasterService;
        this.testOrderService = testOrderService;
        this.sampleCollectionService = sampleCollectionService;
        this.labProcessingService = labProcessingService;
        this.reportVerificationService = reportVerificationService;
        this.labDashboardService = labDashboardService;
        this.labOrderService = labOrderService;
        this.labReportPdfService = labReportPdfService;
        this.labAuditService = labAuditService;
    }

    // ========== Test Master Management (Admin only) ==========

    @PostMapping("/test-masters")
    @PreAuthorize(LabRoleAccess.MANAGE_TEST_MASTER_ROLES)
    public ResponseEntity<TestMasterResponseDto> createTestMaster(
            @Valid @RequestBody TestMasterRequestDto request,
            Authentication auth) {
        String username = auth.getName();
        TestMasterResponseDto created = testMasterService.create(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/test-masters")
    @PreAuthorize("permitAll()")
    public List<TestMasterResponseDto> listTestMasters(@RequestParam(required = false) Boolean active) {
        if (active != null && active) {
            return testMasterService.listActive();
        }
        return testMasterService.listAll();
    }

    @GetMapping("/test-masters/{id}")
    @PreAuthorize("permitAll()")
    public TestMasterResponseDto getTestMaster(@PathVariable Long id) {
        return testMasterService.findById(id);
    }

    @PutMapping("/test-masters/{id}")
    @PreAuthorize(LabRoleAccess.MANAGE_TEST_MASTER_ROLES)
    public TestMasterResponseDto updateTestMaster(
            @PathVariable Long id,
            @Valid @RequestBody TestMasterRequestDto request,
            Authentication auth) {
        String username = auth.getName();
        return testMasterService.update(id, request, username);
    }

    @DeleteMapping("/test-masters/{id}")
    @PreAuthorize(LabRoleAccess.MANAGE_TEST_MASTER_ROLES)
    public ResponseEntity<Void> deleteTestMaster(@PathVariable Long id, Authentication auth) {
        String username = auth.getName();
        testMasterService.softDelete(id, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test-masters/panels/{panelCode}/expand")
    @PreAuthorize("permitAll()")
    public List<String> expandPanel(@PathVariable String panelCode) {
        return testMasterService.expandPanel(panelCode);
    }

    // ========== Lab Tests API (alias for test-master) ==========

    @PostMapping("/tests")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_SUPERVISOR')")
    public ResponseEntity<TestMasterResponseDto> createTest(
            @Valid @RequestBody TestMasterRequestDto request,
            Authentication auth) {
        String username = auth.getName();
        TestMasterResponseDto created = testMasterService.create(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/tests")
    @PreAuthorize("permitAll()")
    public List<TestMasterResponseDto> listTests(@RequestParam(required = false) Boolean active) {
        if (active != null && active) {
            return testMasterService.listActive();
        }
        return testMasterService.listAll();
    }

    @GetMapping("/tests/{id}")
    @PreAuthorize("permitAll()")
    public TestMasterResponseDto getTest(@PathVariable Long id) {
        return testMasterService.findById(id);
    }

    @PutMapping("/tests/{id}")
    @PreAuthorize(LabRoleAccess.MANAGE_TEST_MASTER_ROLES)
    public TestMasterResponseDto updateTest(
            @PathVariable Long id,
            @Valid @RequestBody TestMasterRequestDto request,
            Authentication auth) {
        String username = auth.getName();
        return testMasterService.update(id, request, username);
    }

    @DeleteMapping("/tests/{id}")
    @PreAuthorize(LabRoleAccess.MANAGE_TEST_MASTER_ROLES)
    public ResponseEntity<Void> deleteTest(@PathVariable Long id, Authentication auth) {
        String username = auth.getName();
        testMasterService.softDelete(id, username);
        return ResponseEntity.noContent().build();
    }

    // ========== Test Ordering ==========

    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<LabOrderResponseDto> createOrder(
            @Valid @RequestBody LabOrderRequestDto request,
            Authentication auth) {
        LabOrderResponseDto created = labOrderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/orders")
    @PreAuthorize(LabRoleAccess.VIEW_ORDERS_AND_RESULTS_ROLES)
    public List<LabOrderResponseDto> listOrders(
            @RequestParam(required = false) Long ipdAdmissionId,
            @RequestParam(required = false) Long opdVisitId,
            @RequestParam(required = false) Long patientId) {
        return labOrderService.listOrders(ipdAdmissionId, opdVisitId, patientId);
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize(LabRoleAccess.VIEW_ORDERS_AND_RESULTS_ROLES)
    public LabOrderResponseDto getLabOrder(@PathVariable Long id) {
        return labOrderService.getOrder(id);
    }

    /** Get TestOrder by id (for lab workflow: result entry, report viewer). */
    @GetMapping("/orders/test-order/{id}")
    @PreAuthorize(LabRoleAccess.VIEW_ORDERS_AND_RESULTS_ROLES)
    public TestOrderResponseDto getTestOrder(@PathVariable Long id) {
        return testOrderService.findById(id);
    }

    @GetMapping("/orders/ipd/{ipdAdmissionId}")
    @PreAuthorize(LabRoleAccess.VIEW_ORDERS_AND_RESULTS_ROLES)
    public List<TestOrderResponseDto> getOrdersByIpdAdmission(@PathVariable Long ipdAdmissionId) {
        return testOrderService.findByIpdAdmissionId(ipdAdmissionId);
    }

    @GetMapping("/orders/opd/{opdVisitId}")
    @PreAuthorize("permitAll()")
    public List<TestOrderResponseDto> getOrdersByOpdVisit(@PathVariable Long opdVisitId) {
        return testOrderService.findByOpdVisitId(opdVisitId);
    }

    @GetMapping("/orders/patient/{patientId}")
    @PreAuthorize(LabRoleAccess.VIEW_ORDERS_AND_RESULTS_ROLES)
    public List<TestOrderResponseDto> getOrdersByPatient(@PathVariable Long patientId) {
        return testOrderService.findByPatientId(patientId);
    }

    @GetMapping("/orders/status/{status}")
    @PreAuthorize(LabRoleAccess.LAB_TECHNICIAN_ROLES)
    public List<TestOrderResponseDto> getOrdersByStatus(@PathVariable TestStatus status) {
        return testOrderService.findByStatus(status);
    }

    @GetMapping("/pending/{ipdAdmissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_SUPERVISOR', 'LAB_TECHNICIAN', 'DOCTOR', 'NURSE')")
    public List<DischargePendingItemDto> getPendingByIpd(@PathVariable Long ipdAdmissionId) {
        return testOrderService.getPendingByIpdAdmissionId(ipdAdmissionId);
    }

    // ========== Sample Processing ==========

    @GetMapping("/orders/items/{orderItemId}")
    @PreAuthorize(LabRoleAccess.VIEW_ORDERS_AND_RESULTS_ROLES)
    public LabOrderItemResponseDto getOrderItem(@PathVariable Long orderItemId) {
        return labOrderService.getOrderItem(orderItemId);
    }

    @GetMapping("/sample-processing/pending")
    @PreAuthorize(LabRoleAccess.LAB_TECHNICIAN_ROLES)
    public List<LabOrderItemResponseDto> getPendingProcessingItems() {
        return labOrderService.getPendingProcessingItems();
    }

    /** Process by orderItemId: action START -> IN_PROGRESS, COMPLETE -> COMPLETED. */
    @PutMapping("/sample/process/{orderItemId}")
    @PreAuthorize(LabRoleAccess.LAB_TECHNICIAN_ROLES)
    public ResponseEntity<LabOrderItemResponseDto> processSampleByItem(
            @PathVariable Long orderItemId,
            @RequestParam(defaultValue = "START") String action,
            Authentication auth) {
        LabOrderItemResponseDto dto = labOrderService.processItem(orderItemId, action);
        return ResponseEntity.ok(dto);
    }

    /** Legacy: process by testOrderId (action START only). */
    @PutMapping("/sample/process/test-order/{testOrderId}")
    @PreAuthorize(LabRoleAccess.LAB_TECHNICIAN_ROLES)
    public ResponseEntity<TestOrderResponseDto> processSampleByTestOrder(
            @PathVariable Long testOrderId,
            @RequestParam(defaultValue = "START") String action,
            Authentication auth) {
        String username = auth.getName();
        if ("START".equalsIgnoreCase(action)) {
            labProcessingService.startProcessing(testOrderId, username);
        } else {
            throw new IllegalArgumentException("Supported action: START");
        }
        return ResponseEntity.ok(testOrderService.findById(testOrderId));
    }

    // ========== Sample Collection ==========

    @PostMapping("/samples/collect")
    @PreAuthorize(LabRoleAccess.LAB_TECHNICIAN_ROLES)
    public TestOrderResponseDto collectSample(
            @RequestParam Long testOrderId,
            @Valid @RequestBody SampleCollectionRequestDto request,
            Authentication auth) {
        String username = auth.getName();
        return sampleCollectionService.collectSample(testOrderId, request, username);
    }

    @PostMapping("/samples/reject")
    @PreAuthorize(LabRoleAccess.SAMPLE_REJECT_ROLES)
    public TestOrderResponseDto rejectSample(
            @RequestParam Long testOrderId,
            @RequestParam String rejectionReason,
            Authentication auth) {
        String username = auth.getName();
        return sampleCollectionService.rejectSample(testOrderId, rejectionReason, username);
    }

    // ========== Lab Processing ==========

    /** Enter single result by orderItemId. Fields: testValue, unit, referenceRange, remarks. */
    @PostMapping("/result")
    @PreAuthorize(LabRoleAccess.LAB_TECHNICIAN_ROLES)
    public ResponseEntity<LabResultResponseDto> enterResult(
            @Valid @RequestBody LabResultEntryRequestDto request,
            Authentication auth) {
        String username = auth.getName();
        LabResultResponseDto result = labProcessingService.enterResultByOrderItem(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/result/order-item/{orderItemId}")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'ADMIN')")
    public List<LabResultResponseDto> getResultsByOrderItem(@PathVariable Long orderItemId) {
        return labProcessingService.findByOrderItemId(orderItemId);
    }

    /** Pending verification: items with results entered (COMPLETED), awaiting senior technician/pathologist. */
    @GetMapping("/result-verification/pending")
    @PreAuthorize(LabRoleAccess.RESULT_VERIFICATION_ROLES)
    public List<LabOrderItemResponseDto> getPendingVerificationItems() {
        return labOrderService.getPendingVerificationItems();
    }

    /** Verify or reject result: action VERIFY -> VERIFIED, REJECT -> REJECTED. Only senior technician/pathologist. */
    @PutMapping("/result/verify/{orderItemId}")
    @PreAuthorize(LabRoleAccess.RESULT_VERIFICATION_ROLES)
    public ResponseEntity<LabOrderItemResponseDto> verifyResult(
            @PathVariable Long orderItemId,
            @RequestParam(defaultValue = "VERIFY") String action,
            Authentication auth) {
        String username = auth.getName();
        LabOrderItemResponseDto dto = labOrderService.verifyResult(orderItemId, action, username);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/results")
    @PreAuthorize(LabRoleAccess.LAB_TECHNICIAN_ROLES)
    public ResponseEntity<List<LabResultResponseDto>> enterResults(
            @Valid @RequestBody LabResultRequestDto request,
            Authentication auth) {
        String username = auth.getName();
        List<LabResultResponseDto> results = labProcessingService.enterResults(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    @GetMapping("/results/order/{testOrderId}")
    @PreAuthorize(LabRoleAccess.VIEW_REPORTS_ROLES)
    public List<LabResultResponseDto> getResultsByOrder(@PathVariable Long testOrderId) {
        return labProcessingService.findByTestOrderId(testOrderId);
    }

    // ========== Report Verification & Release ==========

    @PostMapping("/reports/generate")
    @PreAuthorize(LabRoleAccess.LAB_TECHNICIAN_ROLES)
    public ResponseEntity<LabReportResponseDto> generateReport(
            @RequestParam Long testOrderId,
            Authentication auth) {
        String username = auth.getName();
        LabReportResponseDto report = reportVerificationService.generateReport(testOrderId, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @PostMapping("/reports/{reportId}/verify")
    @PreAuthorize("hasAnyRole('LAB_SUPERVISOR', 'ADMIN')")
    public LabReportResponseDto verifyReport(
            @PathVariable Long reportId,
            @RequestParam String supervisorSignature,
            Authentication auth) {
        String username = auth.getName();
        return reportVerificationService.verifyReport(reportId, username, supervisorSignature);
    }

    @PostMapping("/reports/{reportId}/release")
    @PreAuthorize(LabRoleAccess.RESULT_VERIFICATION_ROLES)
    public LabReportResponseDto releaseReport(
            @PathVariable Long reportId,
            Authentication auth) {
        String username = auth.getName();
        return reportVerificationService.releaseReport(reportId, username);
    }

    @GetMapping("/reports/order/{testOrderId}")
    @PreAuthorize(LabRoleAccess.VIEW_REPORTS_ROLES)
    public LabReportResponseDto getReportByOrder(@PathVariable Long testOrderId) {
        return reportVerificationService.findByTestOrderId(testOrderId);
    }

    /** Generate and download PDF report. Includes hospital header, patient details, doctor, results, reference ranges, pathologist signature. */
    @GetMapping(value = "/report/{orderId}/pdf", produces = "application/pdf")
    @PreAuthorize(LabRoleAccess.VIEW_REPORTS_ROLES)
    public ResponseEntity<Resource> getReportPdf(@PathVariable("orderId") Long testOrderId, Authentication auth) {
        try {
            String printedBy = auth != null ? auth.getName() : null;
            byte[] pdf = labReportPdfService.generatePdf(testOrderId, printedBy);
            String filename = "lab-report-" + testOrderId + ".pdf";
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(new ByteArrayResource(pdf));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }

    // ========== Dashboard ==========

    @GetMapping("/dashboard/summary")
    @PreAuthorize(LabRoleAccess.VIEW_ORDERS_AND_RESULTS_ROLES)
    public LabDashboardSummaryDto getDashboardSummary() {
        return labDashboardService.getDashboardSummary();
    }

    @GetMapping("/dashboard/metrics")
    @PreAuthorize(LabRoleAccess.VIEW_ORDERS_AND_RESULTS_ROLES)
    public LabDashboardMetricsDto getDashboardMetrics() {
        return labDashboardService.getMetrics();
    }

    @GetMapping("/dashboard/overview")
    @PreAuthorize(LabRoleAccess.VIEW_ORDERS_AND_RESULTS_ROLES)
    public LabDashboardOverviewDto getDashboardOverview() {
        return labDashboardService.getOverview();
    }

    @GetMapping("/dashboard/today-summary")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'PHLEBOTOMIST', 'ADMIN', 'DOCTOR', 'PHARMACIST', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST')")
    public LabTodaySummaryDto getTodaySummary() {
        return labDashboardService.getTodaySummary();
    }

    @GetMapping("/samples/pending-collection")
    @PreAuthorize(LabRoleAccess.LAB_TECHNICIAN_ROLES)
    public List<TestOrderResponseDto> getPendingCollection() {
        return labDashboardService.getDashboardSummary().getPendingCollection();
    }

    @GetMapping("/samples/emergency")
    @PreAuthorize(LabRoleAccess.LAB_TECHNICIAN_ROLES)
    public List<TestOrderResponseDto> getEmergencySamples() {
        return labDashboardService.getDashboardSummary().getEmergencySamples();
    }

    @GetMapping("/reports/pending-verification")
    @PreAuthorize("hasAnyRole('LAB_SUPERVISOR', 'ADMIN')")
    public List<TestOrderResponseDto> getPendingVerification() {
        return labDashboardService.getDashboardSummary().getPendingVerification();
    }

    @GetMapping("/tat/breaches")
    @PreAuthorize(LabRoleAccess.TAT_BREACHES_ROLES)
    public List<TestOrderResponseDto> getTatBreaches() {
        return labDashboardService.getDashboardSummary().getTatBreaches();
    }

    // ========== Unified Dashboard & Reports Search ==========

    @GetMapping("/dashboard")
    @PreAuthorize(LabRoleAccess.VIEW_ORDERS_AND_RESULTS_ROLES)
    public LabDashboardResponseDto getDashboard() {
        return labDashboardService.getDashboard();
    }

    /** Audit log for a test order (Section 14). */
    @GetMapping("/audit/test-order/{testOrderId}")
    @PreAuthorize(LabRoleAccess.VIEW_ORDERS_AND_RESULTS_ROLES)
    public List<LabAuditLogResponseDto> getAuditByTestOrder(@PathVariable Long testOrderId) {
        return labAuditService.findByTestOrderId(testOrderId).stream()
                .map(this::toAuditDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/reports")
    @PreAuthorize(LabRoleAccess.VIEW_REPORTS_ROLES)
    public List<LabReportResponseDto> searchReports(
            @RequestParam(required = false) String uhid,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String testName,
            @RequestParam(required = false) java.time.LocalDate fromDate,
            @RequestParam(required = false) java.time.LocalDate toDate) {
        return reportVerificationService.searchReports(uhid, patientName, testName, fromDate, toDate);
    }

    private LabAuditLogResponseDto toAuditDto(LabAuditLog log) {
        LabAuditLogResponseDto dto = new LabAuditLogResponseDto();
        dto.setId(log.getId());
        dto.setEventType(log.getEventType());
        dto.setTestOrderId(log.getTestOrderId());
        dto.setLabOrderItemId(log.getLabOrderItemId());
        dto.setLabReportId(log.getLabReportId());
        dto.setOrderNumber(log.getOrderNumber());
        dto.setPerformedBy(log.getPerformedBy());
        dto.setEventAt(log.getEventAt());
        dto.setDetails(log.getDetails());
        return dto;
    }
}
