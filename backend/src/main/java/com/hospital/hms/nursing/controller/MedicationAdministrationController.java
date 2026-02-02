package com.hospital.hms.nursing.controller;

import com.hospital.hms.nursing.dto.MedicationAdministrationRequestDto;
import com.hospital.hms.nursing.dto.MedicationAdministrationResponseDto;
import com.hospital.hms.nursing.service.MedicationAdministrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for medication administration (MAR). Base path: /api (context) + /nursing/medications (mapping).
 * Auth disabled; re-enable @PreAuthorize when auth is on.
 */
@RestController
@RequestMapping("/nursing/medications")
public class MedicationAdministrationController {

    private final MedicationAdministrationService marService;

    public MedicationAdministrationController(MedicationAdministrationService marService) {
        this.marService = marService;
    }

    @PostMapping
    public ResponseEntity<MedicationAdministrationResponseDto> record(@Valid @RequestBody MedicationAdministrationRequestDto request) {
        MedicationAdministrationResponseDto created = marService.record(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{ipdAdmissionId}")
    public ResponseEntity<List<MedicationAdministrationResponseDto>> getByIpdAdmissionId(@PathVariable Long ipdAdmissionId) {
        List<MedicationAdministrationResponseDto> list = marService.findByIpdAdmissionId(ipdAdmissionId);
        return ResponseEntity.ok(list);
    }
}
