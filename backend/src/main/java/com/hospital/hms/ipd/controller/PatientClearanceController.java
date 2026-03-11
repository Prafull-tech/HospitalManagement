package com.hospital.hms.ipd.controller;

import com.hospital.hms.ipd.dto.DischargeStatusDto;
import com.hospital.hms.ipd.dto.PatientClearanceResponseDto;
import com.hospital.hms.ipd.service.DischargeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Patient services clearance API for discharge workflow.
 * GET /api/patient/clearance/{ipdId} returns housekeeping, linen, dietary status.
 * If any false → block discharge.
 */
@RestController
@RequestMapping("/patient/clearance")
public class PatientClearanceController {

    private final DischargeService dischargeService;

    public PatientClearanceController(DischargeService dischargeService) {
        this.dischargeService = dischargeService;
    }

    @GetMapping("/{ipdId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'HOUSEKEEPING', 'LAUNDRY', 'KITCHEN')")
    public ResponseEntity<PatientClearanceResponseDto> getClearance(@PathVariable Long ipdId) {
        DischargeStatusDto status = dischargeService.getStatus(ipdId);
        PatientClearanceResponseDto dto = new PatientClearanceResponseDto();
        dto.setHousekeeping(status.isHousekeepingClearance());
        dto.setLinen(status.isLinenClearance());
        dto.setDietary(status.isDietaryClearance());
        return ResponseEntity.ok(dto);
    }
}
