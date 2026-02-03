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
 * Registers OPD visit. POST /api/opd/visit.
 * Same payload as /api/opd/visits; visit is stored with visitType=OPD.
 */
@RestController
@RequestMapping("/opd")
public class OPDVisitRegistrationController {

    private final OPDVisitService visitService;

    public OPDVisitRegistrationController(OPDVisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping("/visit")
    public ResponseEntity<OPDVisitResponseDto> register(@Valid @RequestBody OPDVisitRequestDto request) {
        OPDVisitResponseDto created = visitService.registerVisit(request, VisitType.OPD);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
