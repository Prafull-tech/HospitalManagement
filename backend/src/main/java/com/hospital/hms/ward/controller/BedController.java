package com.hospital.hms.ward.controller;

import com.hospital.hms.ward.dto.BedRequestDto;
import com.hospital.hms.ward.dto.BedResponseDto;
import com.hospital.hms.ward.dto.BedStatusRequestDto;
import com.hospital.hms.ward.service.BedService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for beds. Base path: /api (context) + /wards for create, /beds for availability and status.
 * Auth disabled; re-enable @PreAuthorize when auth is on.
 */
@RestController
public class BedController {

    private final BedService bedService;

    public BedController(BedService bedService) {
        this.bedService = bedService;
    }

    @PostMapping("/wards/{wardId}/beds")
    public ResponseEntity<BedResponseDto> create(@PathVariable Long wardId,
                                                @Valid @RequestBody BedRequestDto request) {
        BedResponseDto created = bedService.create(wardId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/wards/{wardId}/beds")
    public ResponseEntity<List<BedResponseDto>> listByWardId(@PathVariable Long wardId) {
        List<BedResponseDto> list = bedService.listByWardId(wardId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/beds/availability")
    public ResponseEntity<List<BedResponseDto>> getAvailability(@RequestParam(required = false) Long wardId) {
        List<BedResponseDto> list = bedService.getAvailability(wardId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/beds/{bedId}/status")
    public ResponseEntity<BedResponseDto> updateStatus(@PathVariable Long bedId,
                                                       @Valid @RequestBody BedStatusRequestDto request) {
        BedResponseDto updated = bedService.updateStatus(bedId, request);
        return ResponseEntity.ok(updated);
    }
}
