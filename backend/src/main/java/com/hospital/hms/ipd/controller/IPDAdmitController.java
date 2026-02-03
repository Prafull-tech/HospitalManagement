package com.hospital.hms.ipd.controller;

import com.hospital.hms.ipd.dto.IPDAdmissionRequestDto;
import com.hospital.hms.ipd.dto.IPDAdmissionResponseDto;
import com.hospital.hms.ipd.service.IPDAdmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/ipd/admit — IPD Admit Patient. Bed must be VACANT at submit; bed status set to RESERVED on submit.
 * IPD admission number generated (e.g. IPD-2025-000001), unique.
 */
@RestController
@RequestMapping("/ipd")
public class IPDAdmitController {

    private final IPDAdmissionService admissionService;

    public IPDAdmitController(IPDAdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    @PostMapping("/admit")
    public ResponseEntity<IPDAdmissionResponseDto> admit(@Valid @RequestBody IPDAdmissionRequestDto request) {
        IPDAdmissionResponseDto created = admissionService.admit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
