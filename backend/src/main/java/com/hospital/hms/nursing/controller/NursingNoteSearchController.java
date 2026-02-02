package com.hospital.hms.nursing.controller;

import com.hospital.hms.nursing.dto.NursingNoteSearchResponse;
import com.hospital.hms.nursing.entity.NoteStatus;
import com.hospital.hms.nursing.entity.ShiftType;
import com.hospital.hms.nursing.service.NursingNoteSearchService;
import com.hospital.hms.ward.entity.WardType;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST API for searching nursing notes. Fast, paginated, with q (name/UHID), ward, bed, shift, status, date range.
 * Path: /api/nursing/search/notes
 */
@RestController
@RequestMapping("/nursing/search/notes")
public class NursingNoteSearchController {

    private final NursingNoteSearchService searchService;

    public NursingNoteSearchController(NursingNoteSearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Search nursing notes.
     *
     * @param q        patient name or UHID (partial / fuzzy, case-insensitive)
     * @param wardType General, Private, ICU, CCU, NICU, HDU, etc.
     * @param bedNo    bed number (partial match)
     * @param shift    MORNING, EVENING, NIGHT
     * @param status   DRAFT, LOCKED
     * @param fromDate date from (inclusive)
     * @param toDate   date to (inclusive)
     * @param page     zero-based page index
     * @param size     page size
     */
    @GetMapping
    public ResponseEntity<Page<NursingNoteSearchResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) WardType wardType,
            @RequestParam(required = false) String bedNo,
            @RequestParam(required = false) ShiftType shift,
            @RequestParam(required = false) NoteStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<NursingNoteSearchResponse> result = searchService.search(
                q, wardType, bedNo, shift, status, fromDate, toDate, page, size);
        return ResponseEntity.ok(result);
    }
}
