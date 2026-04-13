package com.hospital.hms.system.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.superadmin.repository.HospitalModuleRepository;
import com.hospital.hms.tenant.service.TenantContextService;
import com.hospital.hms.system.dto.ModuleRequestDto;
import com.hospital.hms.system.dto.ModuleResponseDto;
import com.hospital.hms.system.entity.SystemModule;
import com.hospital.hms.system.repository.SystemModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SystemModuleService {

    private final SystemModuleRepository moduleRepository;
    private final HospitalModuleRepository hospitalModuleRepository;
    private final TenantContextService tenantContextService;

    public SystemModuleService(SystemModuleRepository moduleRepository,
                               HospitalModuleRepository hospitalModuleRepository,
                               TenantContextService tenantContextService) {
        this.moduleRepository = moduleRepository;
        this.hospitalModuleRepository = hospitalModuleRepository;
        this.tenantContextService = tenantContextService;
    }

    public List<ModuleResponseDto> listAll(boolean enabledOnly) {
        Optional<Long> hospitalId = tenantContextService.getCurrentHospitalId();
        List<SystemModule> list = enabledOnly
                ? moduleRepository.findAllByEnabledTrueOrderByModuleCategoryAscSortOrderAscCodeAsc()
                : moduleRepository.findAllByOrderByModuleCategoryAscSortOrderAscCodeAsc();
        return list.stream().map(m -> toDto(m, hospitalId)).collect(Collectors.toList());
    }

    public ModuleResponseDto getById(Long id) {
        Optional<Long> hospitalId = tenantContextService.getCurrentHospitalId();
        SystemModule module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + id));
        return toDto(module, hospitalId);
    }

    @Transactional
    public ModuleResponseDto create(ModuleRequestDto request) {
        if (moduleRepository.findByCode(request.getCode().trim()).isPresent()) {
            throw new IllegalArgumentException("Module code already exists: " + request.getCode());
        }
        SystemModule module = new SystemModule();
        mapToEntity(request, module);
        module = moduleRepository.save(module);
        Optional<Long> hospitalId = tenantContextService.getCurrentHospitalId();
        return toDto(module, hospitalId);
    }

    @Transactional
    public ModuleResponseDto update(Long id, ModuleRequestDto request) {
        Optional<Long> hospitalId = tenantContextService.getCurrentHospitalId();
        SystemModule module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + id));

        String requestedCode = normalizeCode(request.getCode());
        if (request.isEnabled() && isLockedBySuperAdmin(hospitalId, requestedCode)) {
            throw new IllegalArgumentException("Module is not in plan for this hospital and cannot be enabled by Hospital Admin.");
        }

        module.setCode(request.getCode().trim());
        module.setName(request.getName().trim());
        module.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        module.setModuleCategory(request.getModuleCategory());
        module.setRoutePath(request.getRoutePath() != null ? request.getRoutePath().trim() : null);
        module.setEnabled(request.isEnabled());
        module.setSortOrder(request.getSortOrder());
        module = moduleRepository.save(module);
        return toDto(module, hospitalId);
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

    private ModuleResponseDto toDto(SystemModule m, Optional<Long> hospitalId) {
        ModuleResponseDto dto = new ModuleResponseDto();
        dto.setId(m.getId());
        dto.setCode(m.getCode());
        dto.setName(m.getName());
        dto.setDescription(m.getDescription());
        dto.setModuleCategory(m.getModuleCategory());
        dto.setRoutePath(m.getRoutePath());
        dto.setEnabled(m.isEnabled());
        boolean lockedBySuperAdmin = isLockedBySuperAdmin(hospitalId, m.getCode());
        dto.setLockedBySuperAdmin(lockedBySuperAdmin);
        dto.setLockReason(lockedBySuperAdmin ? "NOT_IN_PLAN" : null);
        dto.setSortOrder(m.getSortOrder());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        return dto;
    }

    private boolean isLockedBySuperAdmin(Optional<Long> hospitalId, String moduleCode) {
        if (hospitalId.isEmpty()) {
            return false;
        }
        String normalizedCode = normalizeCode(moduleCode);
        return hospitalModuleRepository.findByHospitalIdAndModuleCode(hospitalId.get(), normalizedCode)
                .map(override -> !override.isEnabled())
                .orElse(false);
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }
}
