package com.hospital.hms.system.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.system.dto.ModuleRequestDto;
import com.hospital.hms.system.dto.ModuleResponseDto;
import com.hospital.hms.system.entity.SystemModule;
import com.hospital.hms.system.repository.SystemModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SystemModuleService {

    private final SystemModuleRepository moduleRepository;

    public SystemModuleService(SystemModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    public List<ModuleResponseDto> listAll(boolean enabledOnly) {
        List<SystemModule> list = enabledOnly
                ? moduleRepository.findAllByEnabledTrueOrderByModuleCategoryAscSortOrderAscCodeAsc()
                : moduleRepository.findAllByOrderByModuleCategoryAscSortOrderAscCodeAsc();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ModuleResponseDto getById(Long id) {
        SystemModule module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + id));
        return toDto(module);
    }

    @Transactional
    public ModuleResponseDto create(ModuleRequestDto request) {
        if (moduleRepository.findByCode(request.getCode().trim()).isPresent()) {
            throw new IllegalArgumentException("Module code already exists: " + request.getCode());
        }
        SystemModule module = new SystemModule();
        mapToEntity(request, module);
        module = moduleRepository.save(module);
        return toDto(module);
    }

    @Transactional
    public ModuleResponseDto update(Long id, ModuleRequestDto request) {
        SystemModule module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + id));
        module.setCode(request.getCode().trim());
        module.setName(request.getName().trim());
        module.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        module.setModuleCategory(request.getModuleCategory());
        module.setRoutePath(request.getRoutePath() != null ? request.getRoutePath().trim() : null);
        module.setEnabled(request.isEnabled());
        module.setSortOrder(request.getSortOrder());
        module = moduleRepository.save(module);
        return toDto(module);
    }

    private void mapToEntity(ModuleRequestDto request, SystemModule module) {
        module.setCode(request.getCode().trim());
        module.setName(request.getName().trim());
        module.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        module.setModuleCategory(request.getModuleCategory());
        module.setRoutePath(request.getRoutePath() != null ? request.getRoutePath().trim() : null);
        module.setEnabled(request.isEnabled());
        module.setSortOrder(request.getSortOrder());
    }

    private ModuleResponseDto toDto(SystemModule m) {
        ModuleResponseDto dto = new ModuleResponseDto();
        dto.setId(m.getId());
        dto.setCode(m.getCode());
        dto.setName(m.getName());
        dto.setDescription(m.getDescription());
        dto.setModuleCategory(m.getModuleCategory());
        dto.setRoutePath(m.getRoutePath());
        dto.setEnabled(m.isEnabled());
        dto.setSortOrder(m.getSortOrder());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        return dto;
    }
}
