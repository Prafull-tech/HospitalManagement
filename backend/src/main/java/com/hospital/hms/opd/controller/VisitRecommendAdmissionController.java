package com.hospital.hms.opd.controller;

import com.hospital.hms.opd.dto.OPDVisitResponseDto;
import com.hospital.hms.opd.dto.RecommendAdmissionRequestDto;
import com.hospital.hms.opd.service.OPDVisitService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PUT /api/visit/{id}/recommend-admission.
 * Only doctor can recommend admission; recommendation is stored with the visit for IPD integration.
 */
@RestController
@RequestMapping("/visit")
public class VisitRecommendAdmissionController {

    private final OPDVisitService visitService;

    public VisitRecommendAdmissionController(OPDVisitService visitService) {
        this.visitService = visitService;
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/{id}/recommend-admission")
    public ResponseEntity<OPDVisitResponseDto> recommendAdmission(
            @PathVariable Long id,
            @RequestBody(required = false) RecommendAdmissionRequestDto request,
            Authentication authentication) {
        OPDVisitResponseDto updated = visitService.recommendAdmission(id, request, authentication);
        return ResponseEntity.ok(updated);
    }
}
