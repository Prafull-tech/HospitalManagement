package com.hospital.hms.nursing.controller;

import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.nursing.dto.NursingNoteRequestDto;
import com.hospital.hms.nursing.dto.NursingNoteResponseDto;
import com.hospital.hms.nursing.entity.NoteStatus;
import com.hospital.hms.nursing.entity.ShiftType;
import com.hospital.hms.nursing.service.NursingNoteService;
import com.hospital.hms.ward.entity.WardType;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.util.List;

/**
 * REST API for nursing notes. Base path: /api (context) + /nursing/notes (mapping).
 * Shift-wise notes, search, lock, print. Method security can be added per role.
 */
@RestController
@RequestMapping("/nursing/notes")
public class NursingNoteController {

    private static final Logger log = LoggerFactory.getLogger(NursingNoteController.class);

    private final NursingNoteService noteService;

    public NursingNoteController(NursingNoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<NursingNoteResponseDto> create(@Valid @RequestBody NursingNoteRequestDto request) {
        MDC.put(MdcKeys.MODULE, "NURSING");
        NursingNoteResponseDto created = noteService.create(request);
        log.info("Nursing note created for ipdAdmissionId={}, noteId={}", request.getIpdAdmissionId(), created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NursingNoteResponseDto> update(@PathVariable Long id,
                                                          @Valid @RequestBody NursingNoteRequestDto request) {
        MDC.put(MdcKeys.MODULE, "NURSING");
        NursingNoteResponseDto updated = noteService.update(id, request);
        log.info("Nursing note updated noteId={}", id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NursingNoteResponseDto> getById(@PathVariable Long id) {
        MDC.put(MdcKeys.MODULE, "NURSING");
        NursingNoteResponseDto dto = noteService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<NursingNoteResponseDto>> search(
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String patientUhid,
            @RequestParam(required = false) String bedNumber,
            @RequestParam(required = false) WardType wardType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordedDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordedDateTo,
            @RequestParam(required = false) ShiftType shiftType,
            @RequestParam(required = false) NoteStatus noteStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        MDC.put(MdcKeys.MODULE, "NURSING");
        Page<NursingNoteResponseDto> result = noteService.search(
                patientName, patientUhid, bedNumber, wardType,
                recordedDateFrom, recordedDateTo, shiftType, noteStatus, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admission/{ipdAdmissionId}")
    public ResponseEntity<List<NursingNoteResponseDto>> getByIpdAdmissionId(@PathVariable Long ipdAdmissionId) {
        MDC.put(MdcKeys.MODULE, "NURSING");
        List<NursingNoteResponseDto> list = noteService.findByIpdAdmissionId(ipdAdmissionId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<NursingNoteResponseDto> lock(@PathVariable Long id,
                                                       @RequestParam(required = false) Long lockedById) {
        MDC.put(MdcKeys.MODULE, "NURSING");
        NursingNoteResponseDto updated = noteService.lock(id, lockedById);
        log.info("Nursing note locked noteId={}", id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/print")
    public ResponseEntity<List<NursingNoteResponseDto>> getPrintData(
            @RequestParam(required = false) Long ipdAdmissionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordedDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordedDateTo,
            @RequestParam(required = false) ShiftType shiftType) {
        MDC.put(MdcKeys.MODULE, "NURSING");
        List<NursingNoteResponseDto> list = noteService.getPrintData(
                ipdAdmissionId, recordedDateFrom, recordedDateTo, shiftType);
        return ResponseEntity.ok(list);
    }
}
