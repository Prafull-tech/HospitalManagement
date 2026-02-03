package com.hospital.hms.reception.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API for reception patient registration and search.
 * Base path: /api (context) + /reception/patients (mapping).
 */
@RestController
@RequestMapping("/reception/patients")
public class ReceptionPatientController {

    private final PatientService patientService;

    public ReceptionPatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    // @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponseDto> register(@Valid @RequestBody PatientRequestDto request) {
        PatientResponseDto created = patientService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<PatientResponseDto> getById(@PathVariable Long id) {
        PatientResponseDto patient = patientService.getById(id);
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/{uhid}")
    // @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'HELP_DESK')")
    public ResponseEntity<PatientResponseDto> getByUhid(@PathVariable String uhid) {
        PatientResponseDto patient = patientService.getByUhid(uhid);
        return ResponseEntity.ok(patient);
    }

    /** List all patients (paginated). Default page=0, size=500. For reception search "all patients below". */
    @GetMapping
    public ResponseEntity<List<PatientResponseDto>> list(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "500") int size) {
        return ResponseEntity.ok(patientService.list(page, size));
    }

    @GetMapping("/search")
    // @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'HELP_DESK')")
    public ResponseEntity<List<PatientResponseDto>> search(
            @RequestParam(required = false) String uhid,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(patientService.searchByQuery(q));
        }
        List<PatientResponseDto> results = patientService.search(uhid, phone, name);
        return ResponseEntity.ok(results);
    }
}
