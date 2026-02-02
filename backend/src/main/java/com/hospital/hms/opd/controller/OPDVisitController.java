package com.hospital.hms.opd.controller;

import com.hospital.hms.opd.dto.*;
import com.hospital.hms.opd.entity.VisitStatus;
import com.hospital.hms.opd.service.OPDVisitService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST API for OPD visits. Base path: /api (context) + /opd/visits (mapping).
 * Auth disabled; re-enable @PreAuthorize when auth is on.
 */
@RestController
@RequestMapping("/opd/visits")
public class OPDVisitController {

    private final OPDVisitService visitService;

    public OPDVisitController(OPDVisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping
    public ResponseEntity<OPDVisitResponseDto> register(@Valid @RequestBody OPDVisitRequestDto request) {
        OPDVisitResponseDto created = visitService.registerVisit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OPDVisitResponseDto> getById(@PathVariable Long id) {
        OPDVisitResponseDto visit = visitService.getById(id);
        return ResponseEntity.ok(visit);
    }

    @GetMapping
    public ResponseEntity<Page<OPDVisitResponseDto>> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) VisitStatus status,
            @RequestParam(required = false) String patientUhid,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String visitNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<OPDVisitResponseDto> result = visitService.search(visitDate, doctorId, status, patientUhid, patientName, visitNumber, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/queue")
    public ResponseEntity<List<OPDVisitResponseDto>> getQueue(
            @RequestParam Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate) {
        List<OPDVisitResponseDto> queue = visitService.getQueue(doctorId, visitDate);
        return ResponseEntity.ok(queue);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OPDVisitResponseDto> updateStatus(@PathVariable Long id,
                                                           @Valid @RequestBody OPDStatusRequestDto request) {
        OPDVisitResponseDto updated = visitService.updateStatus(id, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<OPDClinicalNoteResponseDto> addNotes(@PathVariable Long id,
                                                               @Valid @RequestBody OPDClinicalNoteRequestDto request) {
        OPDClinicalNoteResponseDto note = visitService.addOrUpdateNotes(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }

    @PostMapping("/{id}/refer")
    public ResponseEntity<OPDVisitResponseDto> refer(@PathVariable Long id,
                                                    @Valid @RequestBody OPDReferRequestDto request) {
        OPDVisitResponseDto updated = visitService.refer(id, request);
        return ResponseEntity.ok(updated);
    }
}
