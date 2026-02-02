package com.hospital.hms.hospital.controller;

import com.hospital.hms.hospital.dto.HospitalRequestDto;
import com.hospital.hms.hospital.dto.HospitalResponseDto;
import com.hospital.hms.hospital.service.HospitalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Hospital Master. Base path: /api (context) + /hospitals (mapping).
 * Bed availability CRUD: /api/hospitals/{hospitalId}/beds.
 *
 * Sample request (POST/PUT):
 * { "hospitalCode": "H001", "hospitalName": "Main Hospital", "location": "Central Campus", "active": true }
 *
 * Sample response:
 * { "id": 1, "hospitalCode": "H001", "hospitalName": "Main Hospital", "location": "Central Campus", "active": true }
 */
@RestController
@RequestMapping("/hospitals")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @PostMapping
    public ResponseEntity<HospitalResponseDto> create(@Valid @RequestBody HospitalRequestDto request) {
        HospitalResponseDto created = hospitalService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<HospitalResponseDto>> list(
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {
        List<HospitalResponseDto> list = hospitalService.list(activeOnly);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HospitalResponseDto> getById(@PathVariable Long id) {
        HospitalResponseDto dto = hospitalService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HospitalResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody HospitalRequestDto request) {
        HospitalResponseDto updated = hospitalService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hospitalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
