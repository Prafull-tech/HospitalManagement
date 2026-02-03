package com.hospital.hms.ipd.controller;

import com.hospital.hms.ipd.config.ShiftToWardRoles;
import com.hospital.hms.ipd.dto.IPDAdmissionResponseDto;
import com.hospital.hms.ipd.dto.ShiftToWardRequestDto;
import com.hospital.hms.ipd.service.IPDAdmissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/ipd/{admissionId}/shift-to-ward. Nursing staff performs shift; bed → OCCUPIED, admission status → ACTIVE.
 * Shift timestamp mandatory.
 */
@RestController
@RequestMapping("/ipd")
public class IPDShiftToWardController {

    private final IPDAdmissionService admissionService;

    public IPDShiftToWardController(IPDAdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    @PreAuthorize(ShiftToWardRoles.CAN_SHIFT_TO_WARD)
    @PostMapping("/{admissionId}/shift-to-ward")
    public ResponseEntity<IPDAdmissionResponseDto> shiftToWard(
            @PathVariable Long admissionId,
            @Valid @RequestBody ShiftToWardRequestDto request,
            Authentication authentication) {
        IPDAdmissionResponseDto updated = admissionService.shiftToWard(admissionId, request, authentication);
        return ResponseEntity.ok(updated);
    }
}
