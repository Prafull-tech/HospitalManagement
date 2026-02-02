package com.hospital.hms.doctor.controller;

import com.hospital.hms.doctor.dto.DepartmentRequestDto;
import com.hospital.hms.doctor.dto.DepartmentResponseDto;
import com.hospital.hms.doctor.service.MedicalDepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for medical departments.
 * Base path: /api (context) + /departments (mapping).
 * Auth disabled; re-enable @PreAuthorize when auth is on.
 */
@RestController
@RequestMapping("/departments")
public class DepartmentController {

    private final MedicalDepartmentService departmentService;

    public DepartmentController(MedicalDepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponseDto>> listAll() {
        List<DepartmentResponseDto> list = departmentService.listAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDto> getById(@PathVariable Long id) {
        DepartmentResponseDto dept = departmentService.getById(id);
        return ResponseEntity.ok(dept);
    }

    @PostMapping
    public ResponseEntity<DepartmentResponseDto> create(@Valid @RequestBody DepartmentRequestDto request) {
        DepartmentResponseDto created = departmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponseDto> update(@PathVariable Long id,
                                                         @Valid @RequestBody DepartmentRequestDto request) {
        DepartmentResponseDto updated = departmentService.update(id, request);
        return ResponseEntity.ok(updated);
    }
}
