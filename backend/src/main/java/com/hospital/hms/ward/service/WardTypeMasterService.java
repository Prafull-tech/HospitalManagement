package com.hospital.hms.ward.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.ward.dto.WardTypeMasterRequestDto;
import com.hospital.hms.ward.dto.WardTypeMasterResponseDto;
import com.hospital.hms.ward.entity.WardTypeMaster;
import com.hospital.hms.ward.repository.WardTypeMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Ward Type Master service. Prevents duplicate names (case-insensitive). DB-agnostic.
 */
@Service
public class WardTypeMasterService {

    private final WardTypeMasterRepository repository;

    public WardTypeMasterService(WardTypeMasterRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<WardTypeMasterResponseDto> list(Boolean activeOnly) {
        List<WardTypeMaster> list = Boolean.TRUE.equals(activeOnly)
                ? repository.findByIsActiveTrueOrderByNameAsc()
                : repository.findAllByOrderByNameAsc();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WardTypeMasterResponseDto getById(Long id) {
        WardTypeMaster entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward type not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public WardTypeMasterResponseDto create(WardTypeMasterRequestDto request) {
        String name = request.getName() != null ? request.getName().trim() : "";
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (repository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Ward type name already exists: " + name);
        }
        WardTypeMaster entity = new WardTypeMaster();
        entity.setName(name);
        entity.setIsActive(request.getActive() != null ? request.getActive() : true);
        entity = repository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public WardTypeMasterResponseDto update(Long id, WardTypeMasterRequestDto request) {
        WardTypeMaster entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward type not found: " + id));
        String name = request.getName() != null ? request.getName().trim() : "";
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new IllegalArgumentException("Ward type name already exists: " + name);
        }
        entity.setName(name);
        entity.setIsActive(request.getActive() != null ? request.getActive() : true);
        entity = repository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public void delete(Long id) {
        WardTypeMaster entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward type not found: " + id));
        entity.setIsActive(false);
        repository.save(entity);
    }

    private WardTypeMasterResponseDto toDto(WardTypeMaster entity) {
        WardTypeMasterResponseDto dto = new WardTypeMasterResponseDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setActive(entity.getIsActive());
        return dto;
    }
}
