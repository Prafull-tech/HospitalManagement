package com.hospital.hms.ward.service;

import com.hospital.hms.common.exception.OperationNotAllowedException;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.ward.dto.WardRequestDto;
import com.hospital.hms.ward.dto.WardResponseDto;
import com.hospital.hms.ward.entity.Ward;
import com.hospital.hms.ward.entity.WardType;
import com.hospital.hms.ward.repository.BedRepository;
import com.hospital.hms.ward.repository.WardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Ward service. Single source for ward structure.
 * DB-agnostic (H2 & MySQL).
 */
@Service
public class WardService {

    private final WardRepository wardRepository;
    private final BedRepository bedRepository;
    private final WardRoomAuditService auditService;

    public WardService(WardRepository wardRepository,
                       BedRepository bedRepository,
                       WardRoomAuditService auditService) {
        this.wardRepository = wardRepository;
        this.bedRepository = bedRepository;
        this.auditService = auditService;
    }

    @Transactional
    public WardResponseDto create(WardRequestDto request) {
        String code = request.getCode().trim();
        if (wardRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Ward code already exists: " + code);
        }
        Ward ward = new Ward();
        applyRequest(ward, request, true);
        Ward saved = wardRepository.save(ward);
        WardResponseDto dto = toDto(saved);
        auditService.log("WARD", saved.getId(), "CREATE", null, dto);
        return dto;
    }

    public List<WardResponseDto> list(Boolean activeOnly, WardType wardType) {
        List<Ward> list;
        if (Boolean.TRUE.equals(activeOnly)) {
            list = wardType != null
                    ? wardRepository.findByIsActiveTrueAndWardTypeOrderByNameAsc(wardType)
                    : wardRepository.findByIsActiveTrueOrderByNameAsc();
        } else {
            list = wardRepository.findAll();
            list.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        }
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public WardResponseDto getById(Long id) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found: " + id));
        return toDto(ward);
    }

    public Ward getEntityById(Long id) {
        return wardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found: " + id));
    }

    /**
     * Update ward. ADMIN and IPD_MANAGER allowed (controller enforces roles).
     * ICU/CCU/NICU/HDU type changes restricted to ADMIN (isAdmin flag).
     */
    @Transactional
    public WardResponseDto update(Long id, WardRequestDto request, boolean isAdmin) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found: " + id));

        String newCode = request.getCode().trim();
        wardRepository.findByCode(newCode)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("Ward code already exists: " + newCode);
                });

        boolean wardTypeChanging = request.getWardType() != null
                && !request.getWardType().equals(ward.getWardType());
        if (wardTypeChanging && !isAdmin && isCriticalWardType(request.getWardType())) {
            throw new OperationNotAllowedException("Only ADMIN can change ward type to ICU/CCU/NICU/HDU");
        }

        WardResponseDto before = toDto(ward);
        applyRequest(ward, request, isAdmin);
        Ward saved = wardRepository.save(ward);
        WardResponseDto after = toDto(saved);
        auditService.log("WARD", saved.getId(), "UPDATE", before, after);
        return after;
    }

    /**
     * Soft delete / disable ward. Disallowed when active beds exist.
     */
    @Transactional
    public void disable(Long id) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found: " + id));

        if (bedRepository.existsByWardIdAndIsActiveTrue(id)) {
            throw new OperationNotAllowedException("Ward cannot be disabled while active beds exist");
        }
        if (Boolean.FALSE.equals(ward.getIsActive())) {
            return;
        }
        WardResponseDto before = toDto(ward);
        ward.setIsActive(false);
        Ward saved = wardRepository.save(ward);
        auditService.log("WARD", saved.getId(), "DISABLE", before, toDto(saved));
    }

    private void applyRequest(Ward ward, WardRequestDto request, boolean allowWardTypeChange) {
        ward.setCode(request.getCode().trim());
        ward.setName(request.getName().trim());
        if (allowWardTypeChange && request.getWardType() != null) {
            ward.setWardType(request.getWardType());
        }
        ward.setFloor(request.getFloor());
        ward.setCapacity(request.getCapacity());
        ward.setChargeCategory(trimOrNull(request.getChargeCategory()));
        ward.setRemarks(trimOrNull(request.getRemarks()));
        if (request.getIsActive() != null) {
            ward.setIsActive(request.getIsActive());
        }
    }

    private boolean isCriticalWardType(WardType wt) {
        return wt == WardType.ICU || wt == WardType.CCU
                || wt == WardType.NICU || wt == WardType.HDU;
    }

    private String trimOrNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }

    private WardResponseDto toDto(Ward w) {
        WardResponseDto dto = new WardResponseDto();
        dto.setId(w.getId());
        dto.setCode(w.getCode());
        dto.setName(w.getName());
        dto.setWardType(w.getWardType());
        dto.setFloor(w.getFloor());
        dto.setCapacity(w.getCapacity());
        dto.setChargeCategory(w.getChargeCategory());
        dto.setRemarks(w.getRemarks());
        dto.setIsActive(w.getIsActive());
        dto.setCreatedAt(w.getCreatedAt());
        dto.setUpdatedAt(w.getUpdatedAt());
        return dto;
    }
}
