package com.hospital.hms.opd.controller;

import com.hospital.hms.opd.dto.OPDVisitRequestDto;
import com.hospital.hms.opd.dto.OPDVisitResponseDto;
import com.hospital.hms.opd.entity.VisitType;
import com.hospital.hms.opd.service.OPDVisitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registers Emergency consultation visit. POST /api/emergency/visit.
 * Same payload as OPD (patientUhid, doctorId, visitDate); visit is stored with visitType=EMERGENCY.
 */
@RestController
@RequestMapping("/emergency")
public class EmergencyVisitController {

    private final OPDVisitService visitService;

    public EmergencyVisitController(OPDVisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping("/visit")
    public ResponseEntity<OPDVisitResponseDto> register(@Valid @RequestBody OPDVisitRequestDto request) {
        OPDVisitResponseDto created = visitService.registerVisit(request, VisitType.EMERGENCY);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
