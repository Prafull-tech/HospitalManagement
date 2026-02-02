package com.hospital.hms.nursing.controller;

import com.hospital.hms.nursing.dto.VitalSignRequestDto;
import com.hospital.hms.nursing.dto.VitalSignResponseDto;
import com.hospital.hms.nursing.service.VitalSignService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for vital signs. Base path: /api (context) + /nursing/vitals (mapping).
 * Auth disabled; re-enable @PreAuthorize when auth is on.
 */
@RestController
@RequestMapping("/nursing/vitals")
public class VitalSignController {

    private final VitalSignService vitalSignService;

    public VitalSignController(VitalSignService vitalSignService) {
        this.vitalSignService = vitalSignService;
    }

    @PostMapping
    public ResponseEntity<VitalSignResponseDto> record(@Valid @RequestBody VitalSignRequestDto request) {
        VitalSignResponseDto created = vitalSignService.record(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{ipdAdmissionId}")
    public ResponseEntity<List<VitalSignResponseDto>> getByIpdAdmissionId(@PathVariable Long ipdAdmissionId) {
        List<VitalSignResponseDto> list = vitalSignService.getByIpdAdmissionId(ipdAdmissionId);
        return ResponseEntity.ok(list);
    }
}
