package com.hospital.hms.system.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.system.dto.RoleRequestDto;
import com.hospital.hms.system.dto.RoleResponseDto;
import com.hospital.hms.system.entity.SystemRole;
import com.hospital.hms.system.repository.SystemRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SystemRoleService {

    private final SystemRoleRepository roleRepository;

    public SystemRoleService(SystemRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<RoleResponseDto> listAll(boolean activeOnly) {
        List<SystemRole> list = activeOnly
                ? roleRepository.findAllByActiveTrueOrderBySortOrderAscCodeAsc()
                : roleRepository.findAllByOrderBySortOrderAscCodeAsc();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public RoleResponseDto getById(Long id) {
        SystemRole role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
        return toDto(role);
    }

    @Transactional
    public RoleResponseDto create(RoleRequestDto request) {
        if (roleRepository.findByCode(request.getCode().trim()).isPresent()) {
            throw new IllegalArgumentException("Role code already exists: " + request.getCode());
        }
        SystemRole role = new SystemRole();
        mapToEntity(request, role);
        role = roleRepository.save(role);
        return toDto(role);
    }

    @Transactional
    public RoleResponseDto update(Long id, RoleRequestDto request) {
        SystemRole role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
        if (role.isSystemRole()) {
            if (!role.getCode().equals(request.getCode().trim())) {
                throw new IllegalArgumentException("Cannot change code of system role");
            }
        } else {
            role.setCode(request.getCode().trim());
        }
        role.setName(request.getName().trim());
        role.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        role.setSystemRole(request.isSystemRole());
        role.setActive(request.isActive());
        role.setSortOrder(request.getSortOrder());
        role = roleRepository.save(role);
        return toDto(role);
    }

    private void mapToEntity(RoleRequestDto request, SystemRole role) {
        role.setCode(request.getCode().trim());
        role.setName(request.getName().trim());
        role.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        role.setSystemRole(request.isSystemRole());
        role.setActive(request.isActive());
        role.setSortOrder(request.getSortOrder());
    }

    private RoleResponseDto toDto(SystemRole r) {
        RoleResponseDto dto = new RoleResponseDto();
        dto.setId(r.getId());
        dto.setCode(r.getCode());
        dto.setName(r.getName());
        dto.setDescription(r.getDescription());
        dto.setSystemRole(r.isSystemRole());
        dto.setActive(r.isActive());
        dto.setSortOrder(r.getSortOrder());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}
