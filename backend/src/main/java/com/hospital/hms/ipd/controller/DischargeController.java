package com.hospital.hms.ipd.controller;

import com.hospital.hms.ipd.dto.DischargeStatusDto;
import com.hospital.hms.ipd.dto.DischargeSummaryRequestDto;
import com.hospital.hms.ipd.entity.DischargeType;
import com.hospital.hms.ipd.service.DischargeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for IPD Patient Discharge with real-time clearance tracking.
 * Base path: /api (context) + /ipd/discharge (mapping).
 * NABH / medico-legal compliant.
 */
@RestController
@RequestMapping("/ipd/discharge")
public class DischargeController {

    private final DischargeService dischargeService;

    public DischargeController(DischargeService dischargeService) {
        this.dischargeService = dischargeService;
    }

    @GetMapping("/{ipdAdmissionId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'PHARMACIST', 'IPD_PHARMACIST', 'LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'BILLING')")
    public ResponseEntity<DischargeStatusDto> getStatus(@PathVariable Long ipdAdmissionId) {
        return ResponseEntity.ok(dischargeService.getStatus(ipdAdmissionId));
    }

    @PostMapping("/{ipdAdmissionId}/doctor-clearance")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<DischargeStatusDto> recordDoctorClearance(@PathVariable Long ipdAdmissionId) {
        return ResponseEntity.ok(dischargeService.recordDoctorClearance(ipdAdmissionId));
    }

    @PostMapping("/{ipdAdmissionId}/nursing-clearance")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ResponseEntity<DischargeStatusDto> recordNursingClearance(@PathVariable Long ipdAdmissionId) {
        return ResponseEntity.ok(dischargeService.recordNursingClearance(ipdAdmissionId));
    }

    @PostMapping("/{ipdAdmissionId}/pharmacy-clearance")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'IPD_PHARMACIST', 'PHARMACY_MANAGER', 'STORE_INCHARGE')")
    public ResponseEntity<DischargeStatusDto> recordPharmacyClearance(@PathVariable Long ipdAdmissionId) {
        return ResponseEntity.ok(dischargeService.recordPharmacyClearance(ipdAdmissionId));
    }

    @PostMapping("/{ipdAdmissionId}/lab-clearance")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_TECHNICIAN', 'LAB_SUPERVISOR')")
    public ResponseEntity<DischargeStatusDto> recordLabClearance(@PathVariable Long ipdAdmissionId) {
        return ResponseEntity.ok(dischargeService.recordLabClearance(ipdAdmissionId));
    }

    @PostMapping("/{ipdAdmissionId}/billing-clearance")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING')")
    public ResponseEntity<DischargeStatusDto> recordBillingClearance(@PathVariable Long ipdAdmissionId) {
        return ResponseEntity.ok(dischargeService.recordBillingClearance(ipdAdmissionId));
    }

    @PostMapping("/{ipdAdmissionId}/housekeeping-clearance")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOUSEKEEPING')")
    public ResponseEntity<DischargeStatusDto> recordHousekeepingClearance(@PathVariable Long ipdAdmissionId) {
        return ResponseEntity.ok(dischargeService.recordHousekeepingClearance(ipdAdmissionId));
    }

    @PostMapping("/{ipdAdmissionId}/linen-clearance")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAUNDRY')")
    public ResponseEntity<DischargeStatusDto> recordLinenClearance(@PathVariable Long ipdAdmissionId) {
        return ResponseEntity.ok(dischargeService.recordLinenClearance(ipdAdmissionId));
    }

    @PostMapping("/{ipdAdmissionId}/dietary-clearance")
    @PreAuthorize("hasAnyRole('ADMIN', 'KITCHEN')")
    public ResponseEntity<DischargeStatusDto> recordDietaryClearance(@PathVariable Long ipdAdmissionId) {
        return ResponseEntity.ok(dischargeService.recordDietaryClearance(ipdAdmissionId));
    }

    @PostMapping("/{ipdAdmissionId}/insurance-clearance")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING')")
    public ResponseEntity<DischargeStatusDto> recordInsuranceClearance(
            @PathVariable Long ipdAdmissionId,
            @RequestParam(defaultValue = "false") boolean adminOverride) {
        return ResponseEntity.ok(dischargeService.recordInsuranceClearance(ipdAdmissionId, adminOverride));
    }

    @PostMapping("/{ipdAdmissionId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<DischargeStatusDto> saveDischargeSummary(
            @PathVariable Long ipdAdmissionId,
            @Valid @RequestBody DischargeSummaryRequestDto request) {
        return ResponseEntity.ok(dischargeService.saveDischargeSummary(ipdAdmissionId, request));
    }

    @PostMapping("/{ipdAdmissionId}/finalize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DischargeStatusDto> finalizeDischarge(
            @PathVariable Long ipdAdmissionId,
            @RequestParam(required = false) DischargeType dischargeType) {
        return ResponseEntity.ok(dischargeService.finalizeDischarge(ipdAdmissionId, dischargeType));
    }
}
