package com.hospital.hms.system.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.system.dto.FeatureToggleResponseDto;
import com.hospital.hms.system.entity.FeatureToggle;
import com.hospital.hms.system.repository.FeatureToggleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeatureToggleService {

    private final FeatureToggleRepository featureRepository;

    public FeatureToggleService(FeatureToggleRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    public List<FeatureToggleResponseDto> listAll() {
        return featureRepository.findAllByOrderBySortOrderAscFeatureKeyAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public boolean isEnabled(String featureKey) {
        return featureRepository.findByFeatureKey(featureKey)
                .map(FeatureToggle::isEnabled)
                .orElse(false);
    }

    @Transactional
    public FeatureToggleResponseDto setEnabled(Long id, boolean enabled) {
        FeatureToggle f = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature toggle not found: " + id));
        f.setEnabled(enabled);
        f = featureRepository.save(f);
        return toDto(f);
    }

    private FeatureToggleResponseDto toDto(FeatureToggle f) {
        FeatureToggleResponseDto dto = new FeatureToggleResponseDto();
        dto.setId(f.getId());
        dto.setFeatureKey(f.getFeatureKey());
        dto.setName(f.getName());
        dto.setDescription(f.getDescription());
        dto.setEnabled(f.isEnabled());
        dto.setSortOrder(f.getSortOrder());
        dto.setCreatedAt(f.getCreatedAt());
        dto.setUpdatedAt(f.getUpdatedAt());
        return dto;
    }
}
