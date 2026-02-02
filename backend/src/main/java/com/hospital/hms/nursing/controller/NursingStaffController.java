package com.hospital.hms.nursing.controller;

import com.hospital.hms.nursing.dto.NursingStaffRequestDto;
import com.hospital.hms.nursing.dto.NursingStaffResponseDto;
import com.hospital.hms.nursing.entity.NurseRole;
import com.hospital.hms.nursing.service.NursingStaffService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for nursing staff. Base path: /api (context) + /nursing/staff (mapping).
 * Auth disabled; re-enable @PreAuthorize when auth is on.
 */
@RestController
@RequestMapping("/nursing/staff")
public class NursingStaffController {

    private final NursingStaffService staffService;

    public NursingStaffController(NursingStaffService staffService) {
        this.staffService = staffService;
    }

    @PostMapping
    public ResponseEntity<NursingStaffResponseDto> create(@Valid @RequestBody NursingStaffRequestDto request) {
        NursingStaffResponseDto created = staffService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<NursingStaffResponseDto>> list(
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly,
            @RequestParam(required = false) NurseRole nurseRole) {
        List<NursingStaffResponseDto> list = staffService.list(activeOnly, nurseRole);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NursingStaffResponseDto> getById(@PathVariable Long id) {
        NursingStaffResponseDto staff = staffService.getById(id);
        return ResponseEntity.ok(staff);
    }
}
