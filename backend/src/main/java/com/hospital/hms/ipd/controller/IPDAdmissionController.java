package com.hospital.hms.ipd.controller;

import com.hospital.hms.ipd.config.AdmissionPriorityOverrideRoles;
import com.hospital.hms.ipd.dto.*;
import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.service.IPDAdmissionService;
import com.hospital.hms.ipd.service.IPDAdmissionTimelineService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDateTime;

/**
 * REST API for IPD admissions. Base path: /api (context) + /ipd/admissions (mapping).
 * Priority override: only MEDICAL_SUPERINTENDENT, EMERGENCY_HEAD, IPD_MANAGER (see AdmissionPriorityOverrideRoles).
 */
@RestController
@RequestMapping("/ipd/admissions")
public class IPDAdmissionController {

    private final IPDAdmissionService admissionService;
    private final IPDAdmissionTimelineService timelineService;

    public IPDAdmissionController(IPDAdmissionService admissionService, IPDAdmissionTimelineService timelineService) {
        this.admissionService = admissionService;
        this.timelineService = timelineService;
    }

    @PostMapping
    public ResponseEntity<IPDAdmissionResponseDto> admit(@Valid @RequestBody IPDAdmissionRequestDto request) {
        IPDAdmissionResponseDto created = admissionService.admit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IPDAdmissionResponseDto> getById(@PathVariable Long id) {
        IPDAdmissionResponseDto admission = admissionService.getById(id);
        return ResponseEntity.ok(admission);
    }

    /**
     * Timeline view per patient: all activities linked with this IPD admission (admission, nursing notes, vitals, MAR, charges).
     */
    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<TimelineEventDto>> getTimeline(@PathVariable Long id) {
        List<TimelineEventDto> events = timelineService.getTimeline(id);
        return ResponseEntity.ok(events);
    }

    @GetMapping
    public ResponseEntity<Page<IPDAdmissionResponseDto>> search(
            @RequestParam(required = false) String admissionNumber,
            @RequestParam(required = false) String patientUhid,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) AdmissionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<IPDAdmissionResponseDto> result = admissionService.search(
                admissionNumber, patientUhid, patientName, status, fromDate, toDate, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/ipd/admissions/search — same as GET /api/ipd/admissions with query params.
     * Search by admission number, UHID, patient name, status. Paginated.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<IPDAdmissionResponseDto>> searchAdmissions(
            @RequestParam(required = false) String admissionNumber,
            @RequestParam(required = false) String patientUhid,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) AdmissionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<IPDAdmissionResponseDto> result = admissionService.search(
                admissionNumber, patientUhid, patientName, status, fromDate, toDate, page, size);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<IPDAdmissionResponseDto> transfer(@PathVariable Long id,
                                                            @Valid @RequestBody IPDTransferRequestDto request) {
        IPDAdmissionResponseDto updated = admissionService.transfer(id, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/discharge")
    public ResponseEntity<IPDAdmissionResponseDto> discharge(@PathVariable Long id,
                                                            @Valid @RequestBody IPDDischargeRequestDto request) {
        IPDAdmissionResponseDto updated = admissionService.discharge(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Override admission priority. Only authority roles (MEDICAL_SUPERINTENDENT, EMERGENCY_HEAD, IPD_MANAGER) may call.
     * Override requires a reason (10–500 chars); action is logged for audit.
     */
    @PreAuthorize(AdmissionPriorityOverrideRoles.CAN_OVERRIDE_PRIORITY)
    @PutMapping("/{id}/priority-override")
    public ResponseEntity<IPDAdmissionResponseDto> overridePriority(@PathVariable Long id,
                                                                      @Valid @RequestBody AdmissionPriorityOverrideRequestDto request,
                                                                      Authentication authentication) {
        IPDAdmissionResponseDto updated = admissionService.overridePriority(id, request, authentication);
        return ResponseEntity.ok(updated);
    }
}
