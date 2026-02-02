package com.hospital.hms.ward.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.ward.dto.WardRequestDto;
import com.hospital.hms.ward.dto.WardResponseDto;
import com.hospital.hms.ward.entity.Ward;
import com.hospital.hms.ward.entity.WardType;
import com.hospital.hms.ward.repository.WardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Ward service. Prevents duplicate bed numbers in same ward (handled in BedService). DB-agnostic.
 */
@Service
public class WardService {

    private final WardRepository wardRepository;

    public WardService(WardRepository wardRepository) {
        this.wardRepository = wardRepository;
    }

    @Transactional
    public WardResponseDto create(WardRequestDto request) {
        if (wardRepository.findByCode(request.getCode().trim()).isPresent()) {
            throw new IllegalArgumentException("Ward code already exists: " + request.getCode());
        }
        Ward ward = new Ward();
        ward.setCode(request.getCode().trim());
        ward.setName(request.getName().trim());
        ward.setWardType(request.getWardType());
        ward.setCapacity(request.getCapacity());
        ward.setChargeCategory(request.getChargeCategory() != null ? request.getChargeCategory().trim() : null);
        ward.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        ward = wardRepository.save(ward);
        return toDto(ward);
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

    private WardResponseDto toDto(Ward w) {
        WardResponseDto dto = new WardResponseDto();
        dto.setId(w.getId());
        dto.setCode(w.getCode());
        dto.setName(w.getName());
        dto.setWardType(w.getWardType());
        dto.setCapacity(w.getCapacity());
        dto.setChargeCategory(w.getChargeCategory());
        dto.setIsActive(w.getIsActive());
        dto.setCreatedAt(w.getCreatedAt());
        dto.setUpdatedAt(w.getUpdatedAt());
        return dto;
    }
}
