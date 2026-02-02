package com.hospital.hms.ward.controller;

import com.hospital.hms.ward.dto.WardTypeMasterRequestDto;
import com.hospital.hms.ward.dto.WardTypeMasterResponseDto;
import com.hospital.hms.ward.service.WardTypeMasterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Ward Type Master. Base path: /api (context) + /ward-types (mapping).
 * Configurable ward types; duplicate names prevented (case-insensitive).
 *
 * Sample request (POST/PUT):
 * { "name": "General", "active": true }
 *
 * Sample response:
 * { "id": 1, "name": "General", "active": true }
 */
@RestController
@RequestMapping("/ward-types")
public class WardTypeMasterController {

    private final WardTypeMasterService service;

    public WardTypeMasterController(WardTypeMasterService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<WardTypeMasterResponseDto> create(@Valid @RequestBody WardTypeMasterRequestDto request) {
        WardTypeMasterResponseDto created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<WardTypeMasterResponseDto>> list(
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {
        List<WardTypeMasterResponseDto> list = service.list(activeOnly);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WardTypeMasterResponseDto> getById(@PathVariable Long id) {
        WardTypeMasterResponseDto dto = service.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WardTypeMasterResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody WardTypeMasterRequestDto request) {
        WardTypeMasterResponseDto updated = service.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
