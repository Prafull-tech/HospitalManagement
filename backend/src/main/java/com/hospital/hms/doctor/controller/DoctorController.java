package com.hospital.hms.doctor.controller;

import com.hospital.hms.doctor.dto.*;
import com.hospital.hms.doctor.entity.DoctorStatus;
import com.hospital.hms.doctor.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for doctor / medical staff master.
 * Base path: /api (context) + /doctors (mapping).
 * Auth disabled; re-enable @PreAuthorize when auth is on.
 */
@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    public ResponseEntity<DoctorResponseDto> create(@Valid @RequestBody DoctorRequestDto request) {
        DoctorResponseDto created = doctorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponseDto> update(@PathVariable Long id,
                                                    @Valid @RequestBody DoctorRequestDto request) {
        DoctorResponseDto updated = doctorService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDto> getById(@PathVariable Long id) {
        DoctorResponseDto doctor = doctorService.getById(id);
        return ResponseEntity.ok(doctor);
    }

    @GetMapping
    public ResponseEntity<Page<DoctorResponseDto>> search(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) DoctorStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DoctorResponseDto> result = doctorService.search(code, departmentId, status, search, page, size);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/availability")
    public ResponseEntity<DoctorAvailabilityResponseDto> addAvailability(
            @PathVariable Long id,
            @Valid @RequestBody DoctorAvailabilityRequestDto request) {
        DoctorAvailabilityResponseDto created = doctorService.addAvailability(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<List<DoctorAvailabilityResponseDto>> getAvailability(@PathVariable Long id) {
        List<DoctorAvailabilityResponseDto> list = doctorService.getAvailability(id);
        return ResponseEntity.ok(list);
    }
}
