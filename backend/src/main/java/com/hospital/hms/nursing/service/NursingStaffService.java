package com.hospital.hms.nursing.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.nursing.dto.NursingStaffRequestDto;
import com.hospital.hms.nursing.dto.NursingStaffResponseDto;
import com.hospital.hms.nursing.entity.NurseRole;
import com.hospital.hms.nursing.entity.NursingStaff;
import com.hospital.hms.nursing.repository.NursingStaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Nursing staff service. DB-agnostic.
 */
@Service
public class NursingStaffService {

    private final NursingStaffRepository staffRepository;

    public NursingStaffService(NursingStaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @Transactional
    public NursingStaffResponseDto create(NursingStaffRequestDto request) {
        if (staffRepository.findByCode(request.getCode().trim()).isPresent()) {
            throw new IllegalArgumentException("Nursing staff code already exists: " + request.getCode());
        }
        NursingStaff staff = new NursingStaff();
        staff.setCode(request.getCode().trim());
        staff.setFullName(request.getFullName().trim());
        staff.setNurseRole(request.getNurseRole());
        staff.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        staff.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        staff.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        staff = staffRepository.save(staff);
        return toDto(staff);
    }

    public List<NursingStaffResponseDto> list(Boolean activeOnly, NurseRole nurseRole) {
        List<NursingStaff> list;
        if (Boolean.TRUE.equals(activeOnly)) {
            list = nurseRole != null
                    ? staffRepository.findByIsActiveTrueAndNurseRoleOrderByFullNameAsc(nurseRole)
                    : staffRepository.findByIsActiveTrueOrderByFullNameAsc();
        } else {
            list = staffRepository.findAll();
            list.sort((a, b) -> a.getFullName().compareToIgnoreCase(b.getFullName()));
        }
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public NursingStaffResponseDto getById(Long id) {
        NursingStaff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nursing staff not found: " + id));
        return toDto(staff);
    }

    private NursingStaffResponseDto toDto(NursingStaff s) {
        NursingStaffResponseDto dto = new NursingStaffResponseDto();
        dto.setId(s.getId());
        dto.setCode(s.getCode());
        dto.setFullName(s.getFullName());
        dto.setNurseRole(s.getNurseRole());
        dto.setPhone(s.getPhone());
        dto.setEmail(s.getEmail());
        dto.setIsActive(s.getIsActive());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setUpdatedAt(s.getUpdatedAt());
        return dto;
    }
}
