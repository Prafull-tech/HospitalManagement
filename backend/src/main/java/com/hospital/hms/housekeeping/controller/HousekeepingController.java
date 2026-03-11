package com.hospital.hms.housekeeping.controller;

import com.hospital.hms.housekeeping.dto.HousekeepingTaskRequestDto;
import com.hospital.hms.housekeeping.dto.HousekeepingTaskResponseDto;
import com.hospital.hms.housekeeping.entity.HousekeepingTaskStatus;
import com.hospital.hms.housekeeping.service.HousekeepingTaskService;
import com.hospital.hms.ward.dto.BedStatusRequestDto;
import com.hospital.hms.ward.entity.BedStatus;
import com.hospital.hms.ward.service.BedService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Housekeeping API. Base path: /api (context) + /housekeeping (mapping).
 * <ul>
 *   <li>GET  /api/housekeeping/tasks — List tasks (optional status filter)</li>
 *   <li>POST /api/housekeeping/tasks — Create task</li>
 *   <li>PUT  /api/housekeeping/tasks/{id}/complete — Mark task complete</li>
 *   <li>POST /api/housekeeping/clean/{bedId} — Assign bed cleaning (status → CLEANING)</li>
 *   <li>POST /api/housekeeping/clean/{bedId}/complete — Mark cleaning complete (status → VACANT/AVAILABLE)</li>
 * </ul>
 */
@RestController
@RequestMapping("/housekeeping")
public class HousekeepingController {

    private final BedService bedService;
    private final HousekeepingTaskService taskService;

    public HousekeepingController(BedService bedService, HousekeepingTaskService taskService) {
        this.bedService = bedService;
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<HousekeepingTaskResponseDto>> listTasks(
            @RequestParam(required = false) HousekeepingTaskStatus status) {
        List<HousekeepingTaskResponseDto> tasks = taskService.listTasks(status);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/tasks")
    public ResponseEntity<HousekeepingTaskResponseDto> createTask(@Valid @RequestBody HousekeepingTaskRequestDto request) {
        HousekeepingTaskResponseDto created = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/tasks/{id}/complete")
    public ResponseEntity<HousekeepingTaskResponseDto> completeTask(@PathVariable Long id) {
        HousekeepingTaskResponseDto completed = taskService.completeTask(id);
        return ResponseEntity.ok(completed);
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
