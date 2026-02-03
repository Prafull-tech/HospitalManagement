package com.hospital.hms.reception.controller;

import com.hospital.hms.reception.dto.PatientCardDto;
import com.hospital.hms.reception.dto.PatientRequestDto;
import com.hospital.hms.reception.dto.PatientResponseDto;
import com.hospital.hms.reception.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Patient Registration API at /api/patients.
 * - POST /api/patients — register patient (auto-generates UHID).
 * - GET /api/patients/{uhid} — get patient by UHID.
 * - GET /api/patients/{uhid}/card — get print-ready patient card.
 * Without UHID, IPD admission is not allowed (enforced in IPD admission flow).
 */
@RestController
@RequestMapping("/patients")
public class PatientRegistrationController {

    private final PatientService patientService;

    public PatientRegistrationController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<PatientResponseDto> register(@Valid @RequestBody PatientRequestDto request) {
        PatientResponseDto created = patientService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{uhid}")
    public ResponseEntity<PatientResponseDto> getByUhid(@PathVariable String uhid) {
        PatientResponseDto patient = patientService.getByUhid(uhid.trim());
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/{uhid}/card")
    public ResponseEntity<PatientCardDto> getCard(@PathVariable String uhid) {
        PatientCardDto card = patientService.getCardByUhid(uhid.trim());
        return ResponseEntity.ok(card);
    }
}
