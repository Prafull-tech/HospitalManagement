package com.hospital.hms.nursing.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.nursing.dto.NurseAssignmentRequestDto;
import com.hospital.hms.nursing.dto.NurseAssignmentResponseDto;
import com.hospital.hms.nursing.entity.NurseAssignment;
import com.hospital.hms.nursing.entity.NursingStaff;
import com.hospital.hms.nursing.repository.NurseAssignmentRepository;
import com.hospital.hms.nursing.repository.NursingStaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Nurse assignment service. Only active IPD admissions allowed. DB-agnostic.
 */
@Service
public class NurseAssignmentService {

    private static final List<AdmissionStatus> ACTIVE_STATUSES = Arrays.asList(
            AdmissionStatus.ADMITTED,
            AdmissionStatus.TRANSFERRED,
            AdmissionStatus.DISCHARGE_INITIATED
    );

    private final NurseAssignmentRepository assignmentRepository;
    private final NursingStaffRepository staffRepository;
    private final IPDAdmissionRepository admissionRepository;

    public NurseAssignmentService(NurseAssignmentRepository assignmentRepository,
                                  NursingStaffRepository staffRepository,
                                  IPDAdmissionRepository admissionRepository) {
        this.assignmentRepository = assignmentRepository;
        this.staffRepository = staffRepository;
        this.admissionRepository = admissionRepository;
    }

    @Transactional
    public NurseAssignmentResponseDto create(NurseAssignmentRequestDto request) {
        NursingStaff staff = staffRepository.findById(request.getNursingStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Nursing staff not found: " + request.getNursingStaffId()));
        IPDAdmission admission = admissionRepository.findById(request.getIpdAdmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + request.getIpdAdmissionId()));
        if (!ACTIVE_STATUSES.contains(admission.getAdmissionStatus())) {
            throw new IllegalArgumentException("Only active IPD admissions can have nurse assignments. Current status: " + admission.getAdmissionStatus());
        }
        if (!Boolean.TRUE.equals(staff.getIsActive())) {
            throw new IllegalArgumentException("Nursing staff is not active.");
        }
        NurseAssignment assignment = new NurseAssignment();
        assignment.setNursingStaff(staff);
        assignment.setIpdAdmission(admission);
        assignment.setShiftType(request.getShiftType());
        assignment.setAssignmentDate(request.getAssignmentDate());
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setRemarks(request.getRemarks() != null ? request.getRemarks().trim() : null);
        assignment = assignmentRepository.save(assignment);
        return toDto(assignment);
    }

    public List<NurseAssignmentResponseDto> findByIpdAdmissionId(Long ipdAdmissionId) {
        return assignmentRepository.findByIpdAdmissionIdOrderByAssignmentDateDescShiftTypeAsc(ipdAdmissionId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private NurseAssignmentResponseDto toDto(NurseAssignment a) {
        NurseAssignmentResponseDto dto = new NurseAssignmentResponseDto();
        dto.setId(a.getId());
        dto.setNursingStaffId(a.getNursingStaff().getId());
        dto.setNursingStaffName(a.getNursingStaff().getFullName());
        dto.setNursingStaffCode(a.getNursingStaff().getCode());
        dto.setIpdAdmissionId(a.getIpdAdmission().getId());
        dto.setAdmissionNumber(a.getIpdAdmission().getAdmissionNumber());
        dto.setShiftType(a.getShiftType());
        dto.setAssignmentDate(a.getAssignmentDate());
        dto.setAssignedAt(a.getAssignedAt());
        dto.setRemarks(a.getRemarks());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
