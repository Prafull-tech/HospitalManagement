package com.hospital.hms.nursing.service;

import com.hospital.hms.nursing.dto.NursingNoteSearchResponse;
import com.hospital.hms.nursing.entity.NursingNote;
import com.hospital.hms.nursing.entity.NoteStatus;
import com.hospital.hms.nursing.entity.ShiftType;
import com.hospital.hms.nursing.repository.NursingNoteRepository;
import com.hospital.hms.ward.entity.WardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Search nursing notes with fuzzy patient name/UHID, filters, and sort by latest note time.
 * Role-based restriction (own ward / ward / all) can be applied via security context.
 */
@Service
public class NursingNoteSearchService {

    private final NursingNoteRepository noteRepository;

    public NursingNoteSearchService(NursingNoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    /**
     * Search with optional q (patient name or UHID - fuzzy match on either), wardType, bedNo, shift, status, fromDate, toDate.
     * Case-insensitive, paginated, sorted by note datetime DESC.
     */
    public Page<NursingNoteSearchResponse> search(String q, WardType wardType, String bedNo,
                                                  ShiftType shift, NoteStatus status,
                                                  LocalDate fromDate, LocalDate toDate,
                                                  int page, int size) {
        String qTrimmed = (q != null && !q.isBlank()) ? q.trim() : null;
        String bedNumber = (bedNo != null && !bedNo.isBlank()) ? bedNo.trim() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "recordedAt"));
        Page<NursingNote> result = noteRepository.searchWithQ(
                qTrimmed,
                bedNumber,
                wardType,
                fromDate,
                toDate,
                shift,
                status,
                pageable
        );
        return result.map(this::toSearchDto);
    }

    private NursingNoteSearchResponse toSearchDto(NursingNote n) {
        NursingNoteSearchResponse dto = new NursingNoteSearchResponse();
        dto.setNoteId(n.getId());
        dto.setIpdAdmissionId(n.getIpdAdmission().getId());
        dto.setPatientName(n.getPatientName());
        dto.setUhid(n.getPatientUhid());
        dto.setWardType(n.getWardType());
        dto.setWardName(n.getWardName());
        dto.setBedNo(n.getBedNumber());
        dto.setShift(n.getShiftType());
        dto.setNoteDateTime(n.getRecordedAt());
        dto.setStatus(n.getNoteStatus());
        dto.setNoteType(n.getNoteType());
        dto.setContent(n.getContent());
        if (n.getRecordedBy() != null) {
            dto.setRecordedByName(n.getRecordedBy().getFullName());
        }
        if (n.getUpdatedAt() != null) {
            dto.setLastUpdated(LocalDateTime.ofInstant(n.getUpdatedAt(), ZoneId.systemDefault()));
        } else if (n.getCreatedAt() != null) {
            dto.setLastUpdated(LocalDateTime.ofInstant(n.getCreatedAt(), ZoneId.systemDefault()));
        } else {
            dto.setLastUpdated(n.getRecordedAt());
        }
        return dto;
    }
}
