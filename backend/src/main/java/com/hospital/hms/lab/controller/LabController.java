package com.hospital.hms.lab.controller;

import com.hospital.hms.lab.dto.LabDashboardMetricsDto;
import com.hospital.hms.lab.dto.LabDashboardOverviewDto;
import com.hospital.hms.lab.dto.LabDashboardSummaryDto;
import com.hospital.hms.lab.dto.LabReportResponseDto;
import com.hospital.hms.lab.dto.LabResultRequestDto;
import com.hospital.hms.lab.dto.LabResultResponseDto;
import com.hospital.hms.lab.dto.LabTodaySummaryDto;
import com.hospital.hms.lab.dto.SampleCollectionRequestDto;
import com.hospital.hms.lab.dto.TestMasterRequestDto;
import com.hospital.hms.lab.dto.TestMasterResponseDto;
import com.hospital.hms.ipd.dto.DischargePendingItemDto;
import com.hospital.hms.lab.dto.TestOrderRequestDto;
import com.hospital.hms.lab.dto.TestOrderResponseDto;
import com.hospital.hms.lab.entity.TestStatus;
import com.hospital.hms.lab.service.*;
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

    public LabController(
            TestMasterService testMasterService,
            TestOrderService testOrderService,
            SampleCollectionService sampleCollectionService,
            LabProcessingService labProcessingService,
            ReportVerificationService reportVerificationService,
            LabDashboardService labDashboardService) {
        this.testMasterService = testMasterService;
        this.testOrderService = testOrderService;
        this.sampleCollectionService = sampleCollectionService;
        this.labProcessingService = labProcessingService;
        this.reportVerificationService = reportVerificationService;
        this.labDashboardService = labDashboardService;
    }

    // ========== Test Master Management ==========

    @PostMapping("/test-masters")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_SUPERVISOR')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_SUPERVISOR')")
    public TestMasterResponseDto updateTestMaster(
            @PathVariable Long id,
            @Valid @RequestBody TestMasterRequestDto request,
            Authentication auth) {
        String username = auth.getName();
        return testMasterService.update(id, request, username);
    }

    @DeleteMapping("/test-masters/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_SUPERVISOR')")
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

    // ========== Test Ordering ==========

    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<TestOrderResponseDto>> createOrder(
            @Valid @RequestBody TestOrderRequestDto request,
            Authentication auth) {
        String username = auth.getName();
        List<TestOrderResponseDto> created = testOrderService.createOrder(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("permitAll()")
    public TestOrderResponseDto getOrder(@PathVariable Long id) {
        return testOrderService.findById(id);
    }

    @GetMapping("/orders/ipd/{ipdAdmissionId}")
    @PreAuthorize("permitAll()")
    public List<TestOrderResponseDto> getOrdersByIpdAdmission(@PathVariable Long ipdAdmissionId) {
        return testOrderService.findByIpdAdmissionId(ipdAdmissionId);
    }

    @GetMapping("/orders/opd/{opdVisitId}")
    @PreAuthorize("permitAll()")
    public List<TestOrderResponseDto> getOrdersByOpdVisit(@PathVariable Long opdVisitId) {
        return testOrderService.findByOpdVisitId(opdVisitId);
    }

    @GetMapping("/orders/patient/{patientId}")
    @PreAuthorize("permitAll()")
    public List<TestOrderResponseDto> getOrdersByPatient(@PathVariable Long patientId) {
        return testOrderService.findByPatientId(patientId);
    }

    @GetMapping("/orders/status/{status}")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'PHLEBOTOMIST', 'ADMIN')")
    public List<TestOrderResponseDto> getOrdersByStatus(@PathVariable TestStatus status) {
        return testOrderService.findByStatus(status);
    }

    @GetMapping("/pending/{ipdAdmissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_SUPERVISOR', 'LAB_TECHNICIAN', 'DOCTOR', 'NURSE')")
    public List<DischargePendingItemDto> getPendingByIpd(@PathVariable Long ipdAdmissionId) {
        return testOrderService.getPendingByIpdAdmissionId(ipdAdmissionId);
    }

    // ========== Sample Collection ==========

    @PostMapping("/samples/collect")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST', 'LAB_TECHNICIAN', 'ADMIN')")
    public TestOrderResponseDto collectSample(
            @RequestParam Long testOrderId,
            @Valid @RequestBody SampleCollectionRequestDto request,
            Authentication auth) {
        String username = auth.getName();
        return sampleCollectionService.collectSample(testOrderId, request, username);
    }

    @PostMapping("/samples/reject")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST', 'LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'ADMIN')")
    public TestOrderResponseDto rejectSample(
            @RequestParam Long testOrderId,
            @RequestParam String rejectionReason,
            Authentication auth) {
        String username = auth.getName();
        return sampleCollectionService.rejectSample(testOrderId, rejectionReason, username);
    }

    // ========== Lab Processing ==========

    @PostMapping("/results")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN', 'ADMIN')")
    public ResponseEntity<List<LabResultResponseDto>> enterResults(
            @Valid @RequestBody LabResultRequestDto request,
            Authentication auth) {
        String username = auth.getName();
        List<LabResultResponseDto> results = labProcessingService.enterResults(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    @GetMapping("/results/order/{testOrderId}")
    @PreAuthorize("permitAll()")
    public List<LabResultResponseDto> getResultsByOrder(@PathVariable Long testOrderId) {
        return labProcessingService.findByTestOrderId(testOrderId);
    }

    // ========== Report Verification & Release ==========

    @PostMapping("/reports/generate")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN', 'ADMIN')")
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
    @PreAuthorize("hasAnyRole('LAB_SUPERVISOR', 'ADMIN')")
    public LabReportResponseDto releaseReport(
            @PathVariable Long reportId,
            Authentication auth) {
        String username = auth.getName();
        return reportVerificationService.releaseReport(reportId, username);
    }

    @GetMapping("/reports/order/{testOrderId}")
    @PreAuthorize("permitAll()")
    public LabReportResponseDto getReportByOrder(@PathVariable Long testOrderId) {
        return reportVerificationService.findByTestOrderId(testOrderId);
    }

    // ========== Dashboard ==========

    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'PHLEBOTOMIST', 'ADMIN', 'DOCTOR', 'PHARMACIST', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST')")
    public LabDashboardSummaryDto getDashboardSummary() {
        return labDashboardService.getDashboardSummary();
    }

    @GetMapping("/dashboard/metrics")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'PHLEBOTOMIST', 'ADMIN', 'DOCTOR', 'PHARMACIST', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST')")
    public LabDashboardMetricsDto getDashboardMetrics() {
        return labDashboardService.getMetrics();
    }

    @GetMapping("/dashboard/overview")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'PHLEBOTOMIST', 'ADMIN', 'DOCTOR', 'PHARMACIST', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST')")
    public LabDashboardOverviewDto getDashboardOverview() {
        return labDashboardService.getOverview();
    }

    @GetMapping("/dashboard/today-summary")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'PHLEBOTOMIST', 'ADMIN', 'DOCTOR', 'PHARMACIST', 'PHARMACY_MANAGER', 'STORE_INCHARGE', 'IPD_PHARMACIST')")
    public LabTodaySummaryDto getTodaySummary() {
        return labDashboardService.getTodaySummary();
    }

    @GetMapping("/samples/pending-collection")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST', 'LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'ADMIN')")
    public List<TestOrderResponseDto> getPendingCollection() {
        return labDashboardService.getDashboardSummary().getPendingCollection();
    }

    @GetMapping("/samples/emergency")
    @PreAuthorize("hasAnyRole('PHLEBOTOMIST', 'LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'ADMIN')")
    public List<TestOrderResponseDto> getEmergencySamples() {
        return labDashboardService.getDashboardSummary().getEmergencySamples();
    }

    @GetMapping("/reports/pending-verification")
    @PreAuthorize("hasAnyRole('LAB_SUPERVISOR', 'ADMIN')")
    public List<TestOrderResponseDto> getPendingVerification() {
        return labDashboardService.getDashboardSummary().getPendingVerification();
    }

    @GetMapping("/tat/breaches")
    @PreAuthorize("hasAnyRole('LAB_SUPERVISOR', 'ADMIN', 'DOCTOR', 'QUALITY_MANAGER')")
    public List<TestOrderResponseDto> getTatBreaches() {
        return labDashboardService.getDashboardSummary().getTatBreaches();
    }
}
