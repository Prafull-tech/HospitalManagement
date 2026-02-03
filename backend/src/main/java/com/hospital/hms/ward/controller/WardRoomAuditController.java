package com.hospital.hms.ward.controller;

import com.hospital.hms.ward.entity.WardRoomAuditLog;
import com.hospital.hms.ward.repository.WardRoomAuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read-only API for Ward & Room audit trail.
 * Base path: /api (context) + /audit/ward-room (mapping).
 */
@RestController
@RequestMapping("/audit/ward-room")
public class WardRoomAuditController {

    private final WardRoomAuditLogRepository repository;

    public WardRoomAuditController(WardRoomAuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WardRoomAuditLog>> list(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId) {
        List<WardRoomAuditLog> logs = (entityType != null && entityId != null)
                ? repository.findByEntityTypeAndEntityIdOrderByPerformedAtDesc(entityType, entityId)
                : repository.findAllByOrderByPerformedAtDesc();
        return ResponseEntity.ok(logs);
    }
}

