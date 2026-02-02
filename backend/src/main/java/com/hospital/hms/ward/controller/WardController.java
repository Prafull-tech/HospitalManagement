package com.hospital.hms.ward.controller;

import com.hospital.hms.ward.dto.WardRequestDto;
import com.hospital.hms.ward.dto.WardResponseDto;
import com.hospital.hms.ward.entity.WardType;
import com.hospital.hms.ward.service.WardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for wards. Base path: /api (context) + /wards (mapping).
 * Auth disabled; re-enable @PreAuthorize when auth is on.
 */
@RestController
@RequestMapping("/wards")
public class WardController {

    private final WardService wardService;

    public WardController(WardService wardService) {
        this.wardService = wardService;
    }

    @PostMapping
    public ResponseEntity<WardResponseDto> create(@Valid @RequestBody WardRequestDto request) {
        WardResponseDto created = wardService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<WardResponseDto>> list(
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly,
            @RequestParam(required = false) WardType wardType) {
        List<WardResponseDto> list = wardService.list(activeOnly, wardType);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WardResponseDto> getById(@PathVariable Long id) {
        WardResponseDto ward = wardService.getById(id);
        return ResponseEntity.ok(ward);
    }
}
