package com.hospital.hms.nursing.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.BedAllocationRepository;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.nursing.dto.NursingNoteRequestDto;
import com.hospital.hms.nursing.dto.NursingNoteResponseDto;
import com.hospital.hms.nursing.entity.NoteStatus;
import com.hospital.hms.nursing.entity.NursingNote;
import com.hospital.hms.nursing.entity.NursingStaff;
import com.hospital.hms.nursing.entity.ShiftType;
import com.hospital.hms.nursing.repository.NursingNoteRepository;
import com.hospital.hms.nursing.repository.NursingStaffRepository;
import com.hospital.hms.ward.entity.WardType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Nursing notes service. Shift-wise notes, ward-specific rules, locking, audit. DB-agnostic.
 */
@Service
public class NursingNoteService {

    private static final Logger log = LoggerFactory.getLogger(NursingNoteService.class);

    private static final List<AdmissionStatus> ACTIVE_STATUSES = Arrays.asList(
            AdmissionStatus.ADMITTED,
            AdmissionStatus.TRANSFERRED,
            AdmissionStatus.DISCHARGE_INITIATED
    );

    private final NursingNoteRepository noteRepository;
    private final IPDAdmissionRepository admissionRepository;
    private final BedAllocationRepository bedAllocationRepository;
    private final NursingStaffRepository staffRepository;

    public NursingNoteService(NursingNoteRepository noteRepository,
                              IPDAdmissionRepository admissionRepository,
                              BedAllocationRepository bedAllocationRepository,
                              NursingStaffRepository staffRepository) {
        this.noteRepository = noteRepository;
        this.admissionRepository = admissionRepository;
        this.bedAllocationRepository = bedAllocationRepository;
        this.staffRepository = staffRepository;
    }

    /** Current shift by time: MORNING 06–14, EVENING 14–22, NIGHT 22–06. */
    public static ShiftType getCurrentShift(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        if (hour >= 6 && hour < 14) return ShiftType.MORNING;
        if (hour >= 14 && hour < 22) return ShiftType.EVENING;
        return ShiftType.NIGHT;
    }

    @Transactional
    public NursingNoteResponseDto create(NursingNoteRequestDto request) {
        MDC.put(MdcKeys.MODULE, "NURSING");
        IPDAdmission admission = admissionRepository.findById(request.getIpdAdmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + request.getIpdAdmissionId()));
        if (!ACTIVE_STATUSES.contains(admission.getAdmissionStatus())) {
            log.error("Nursing note create failed for ipdAdmissionId={} - admission not active, status={}",
                    request.getIpdAdmissionId(), admission.getAdmissionStatus());
            throw new IllegalArgumentException("Nursing notes can only be added for active IPD admissions. Current status: " + admission.getAdmissionStatus());
        }
        LocalDateTime recordedAt = request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now();
        ShiftType shiftType = request.getShiftType() != null ? request.getShiftType() : getCurrentShift(recordedAt);

        NursingNote note = new NursingNote();
        note.setIpdAdmission(admission);
        note.setShiftType(shiftType);
        note.setNoteType(request.getNoteType().trim());
        note.setContent(request.getContent().trim());
        note.setRecordedAt(recordedAt);
        note.setRecordedDate(recordedAt.toLocalDate());
        note.setNoteStatus(NoteStatus.DRAFT);
        if (request.getRecordedById() != null) {
            NursingStaff staff = staffRepository.findById(request.getRecordedById()).orElse(null);
            note.setRecordedBy(staff);
        }
        if (request.getCriticalFlags() != null && !request.getCriticalFlags().isBlank()) {
            note.setCriticalFlags(request.getCriticalFlags().trim());
        }

        note.setPatientName(admission.getPatient().getFullName());
        note.setPatientUhid(admission.getPatient().getUhid());
        final NursingNote noteRef = note;
        bedAllocationRepository.findActiveByAdmissionId(admission.getId()).ifPresent(alloc -> {
            noteRef.setWardType(alloc.getBed().getWard().getWardType());
            noteRef.setWardName(alloc.getBed().getWard().getName());
            noteRef.setBedNumber(alloc.getBed().getBedNumber());
        });

        note = noteRepository.save(note);
        log.info("Nursing note saved for ipdAdmissionId={}, noteId={}", request.getIpdAdmissionId(), note.getId());
        return toDto(note);
    }

    @Transactional
    public NursingNoteResponseDto update(Long id, NursingNoteRequestDto request) {
        MDC.put(MdcKeys.MODULE, "NURSING");
        NursingNote note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nursing note not found: " + id));
        if (note.getNoteStatus() == NoteStatus.LOCKED) {
            log.error("Nursing note update failed for noteId={} - note is locked", id);
            throw new IllegalArgumentException("Locked notes cannot be edited.");
        }
        note.setNoteType(request.getNoteType().trim());
        note.setContent(request.getContent().trim());
        if (request.getShiftType() != null) note.setShiftType(request.getShiftType());
        if (request.getCriticalFlags() != null) note.setCriticalFlags(request.getCriticalFlags().trim().isEmpty() ? null : request.getCriticalFlags().trim());
        note = noteRepository.save(note);
        return toDto(note);
    }

    public NursingNoteResponseDto getById(Long id) {
        NursingNote note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nursing note not found: " + id));
        return toDto(note);
    }

    public Page<NursingNoteResponseDto> search(String patientName, String patientUhid, String bedNumber,
                                                WardType wardType, LocalDate recordedDateFrom, LocalDate recordedDateTo,
                                                ShiftType shiftType, NoteStatus noteStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "recordedAt"));
        Page<NursingNote> result = noteRepository.search(
                patientName != null && !patientName.isBlank() ? patientName.trim() : null,
                patientUhid != null && !patientUhid.isBlank() ? patientUhid.trim() : null,
                bedNumber != null && !bedNumber.isBlank() ? bedNumber.trim() : null,
                wardType,
                recordedDateFrom,
                recordedDateTo,
                shiftType,
                noteStatus,
                pageable
        );
        return result.map(this::toDto);
    }

    public List<NursingNoteResponseDto> findByIpdAdmissionId(Long ipdAdmissionId) {
        if (!admissionRepository.existsById(ipdAdmissionId)) {
            throw new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId);
        }
        return noteRepository.findByIpdAdmissionIdOrderByRecordedAtDesc(ipdAdmissionId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public NursingNoteResponseDto lock(Long id, Long lockedById) {
        NursingNote note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nursing note not found: " + id));
        if (note.getNoteStatus() == NoteStatus.LOCKED) {
            return toDto(note);
        }
        note.setNoteStatus(NoteStatus.LOCKED);
        note.setLockedAt(LocalDateTime.now());
        if (lockedById != null) {
            NursingStaff staff = staffRepository.findById(lockedById).orElse(null);
            note.setLockedBy(staff);
        }
        note = noteRepository.save(note);
        log.info("Nursing note locked noteId={}", id);
        return toDto(note);
    }

    public List<NursingNoteResponseDto> getPrintData(Long ipdAdmissionId, LocalDate recordedDateFrom,
                                                      LocalDate recordedDateTo, ShiftType shiftType) {
        if (ipdAdmissionId != null) {
            return findByIpdAdmissionId(ipdAdmissionId);
        }
        Page<NursingNoteResponseDto> page = search(null, null, null, null,
                recordedDateFrom, recordedDateTo, shiftType, null, 0, 500);
        return page.getContent();
    }

    private NursingNoteResponseDto toDto(NursingNote n) {
        NursingNoteResponseDto dto = new NursingNoteResponseDto();
        dto.setId(n.getId());
        dto.setIpdAdmissionId(n.getIpdAdmission().getId());
        dto.setAdmissionNumber(n.getIpdAdmission().getAdmissionNumber());
        dto.setShiftType(n.getShiftType());
        dto.setNoteType(n.getNoteType());
        dto.setContent(n.getContent());
        dto.setRecordedAt(n.getRecordedAt());
        dto.setNoteStatus(n.getNoteStatus());
        dto.setLockedAt(n.getLockedAt());
        dto.setCriticalFlags(n.getCriticalFlags());
        dto.setWardType(n.getWardType());
        dto.setWardName(n.getWardName());
        dto.setBedNumber(n.getBedNumber());
        dto.setPatientName(n.getPatientName());
        dto.setPatientUhid(n.getPatientUhid());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setUpdatedAt(n.getUpdatedAt());
        if (n.getRecordedBy() != null) {
            dto.setRecordedById(n.getRecordedBy().getId());
            dto.setRecordedByName(n.getRecordedBy().getFullName());
        }
        if (n.getLockedBy() != null) {
            dto.setLockedById(n.getLockedBy().getId());
            dto.setLockedByName(n.getLockedBy().getFullName());
        }
        return dto;
    }
}
