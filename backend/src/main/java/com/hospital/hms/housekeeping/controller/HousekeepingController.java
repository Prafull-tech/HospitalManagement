package com.hospital.hms.housekeeping.controller;

import com.hospital.hms.ward.dto.BedStatusRequestDto;
import com.hospital.hms.ward.entity.BedStatus;
import com.hospital.hms.ward.service.BedService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * POST /api/housekeeping/clean/{bedId} — Assign bed cleaning task (status → CLEANING).
 * POST /api/housekeeping/clean/{bedId}/complete — Mark cleaning complete (status → VACANT/AVAILABLE).
 */
@RestController
@RequestMapping("/housekeeping")
public class HousekeepingController {

    private final BedService bedService;

    public HousekeepingController(BedService bedService) {
        this.bedService = bedService;
    }

    @PostMapping("/clean/{bedId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOUSEKEEPING', 'NURSE')")
    public ResponseEntity<Map<String, Object>> assignCleaningTask(@PathVariable Long bedId) {
        BedStatusRequestDto req = new BedStatusRequestDto();
        req.setBedStatus(BedStatus.CLEANING);
        bedService.updateStatus(bedId, req);
        return ResponseEntity.ok(Map.of(
                "bedId", bedId,
                "status", "CLEANING",
                "message", "Bed cleaning task assigned"));
    }

    @PostMapping("/clean/{bedId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOUSEKEEPING', 'NURSE')")
    public ResponseEntity<Map<String, Object>> completeCleaning(@PathVariable Long bedId) {
        BedStatusRequestDto req = new BedStatusRequestDto();
        req.setBedStatus(BedStatus.AVAILABLE);
        bedService.updateStatus(bedId, req);
        return ResponseEntity.ok(Map.of(
                "bedId", bedId,
                "status", "VACANT",
                "message", "Bed cleaning complete"));
    }
}
