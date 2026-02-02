package com.hospital.hms.nursing.controller;

import com.hospital.hms.nursing.dto.NurseAssignmentRequestDto;
import com.hospital.hms.nursing.dto.NurseAssignmentResponseDto;
import com.hospital.hms.nursing.service.NurseAssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for nurse assignments. Base path: /api (context) + /nursing/assignments (mapping).
 * Auth disabled; re-enable @PreAuthorize when auth is on.
 */
@RestController
@RequestMapping("/nursing/assignments")
public class NurseAssignmentController {

    private final NurseAssignmentService assignmentService;

    public NurseAssignmentController(NurseAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public ResponseEntity<NurseAssignmentResponseDto> create(@Valid @RequestBody NurseAssignmentRequestDto request) {
        NurseAssignmentResponseDto created = assignmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/by-admission/{ipdAdmissionId}")
    public ResponseEntity<List<NurseAssignmentResponseDto>> getByIpdAdmissionId(@PathVariable Long ipdAdmissionId) {
        List<NurseAssignmentResponseDto> list = assignmentService.findByIpdAdmissionId(ipdAdmissionId);
        return ResponseEntity.ok(list);
    }
}
