package com.hospital.hms.system.service;

import com.hospital.hms.system.dto.*;
import com.hospital.hms.system.entity.*;
import com.hospital.hms.system.repository.RoleModulePermissionRepository;
import com.hospital.hms.system.repository.SystemModuleRepository;
import com.hospital.hms.system.repository.SystemRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final RoleModulePermissionRepository permissionRepository;
    private final SystemRoleRepository roleRepository;
    private final SystemModuleRepository moduleRepository;

    public PermissionService(RoleModulePermissionRepository permissionRepository,
                             SystemRoleRepository roleRepository,
                             SystemModuleRepository moduleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.moduleRepository = moduleRepository;
    }

    /**
     * Resolve role codes to role IDs. Used when auth sends role names (e.g. from JWT).
     */
    public List<Long> resolveRoleIdsByCodes(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) return Collections.emptyList();
        return roleRepository.findAll().stream()
                .filter(r -> roleCodes.contains(r.getCode()))
                .map(SystemRole::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Effective permissions and allowed modules for the given role codes (e.g. current user).
     * When no roles provided, returns empty (safe for unauthenticated).
     */
    @Transactional(readOnly = true)
    public MyPermissionsResponseDto getMyPermissions(List<String> roleCodes) {
        MyPermissionsResponseDto response = new MyPermissionsResponseDto();
        response.setRoleCodes(roleCodes != null ? new ArrayList<>(roleCodes) : Collections.emptyList());

        if (roleCodes == null || roleCodes.isEmpty()) {
            response.setAllowedModules(Collections.emptyList());
            response.setPermissions(Collections.emptyList());
            return response;
        }

        List<Long> roleIds = resolveRoleIdsByCodes(roleCodes);
        if (roleIds.isEmpty()) {
            response.setAllowedModules(Collections.emptyList());
            response.setPermissions(Collections.emptyList());
            return response;
        }

        List<RoleModulePermission> list = permissionRepository.findByRoleIdInWithModule(roleIds);
        Set<Long> allowedModuleIds = new HashSet<>();
        Map<Long, Set<ActionType>> moduleActions = new HashMap<>();
        Map<Long, ModuleVisibility> moduleVisibility = new HashMap<>();

        for (RoleModulePermission rmp : list) {
            Long mid = rmp.getModule().getId();
            allowedModuleIds.add(mid);
            moduleActions.computeIfAbsent(mid, k -> new HashSet<>()).add(rmp.getActionType());
            if (rmp.getVisibility() != null) {
                moduleVisibility.put(mid, rmp.getVisibility());
            }
        }

        List<ModuleResponseDto> allowedModules = moduleRepository.findAllById(allowedModuleIds).stream()
                .filter(SystemModule::isEnabled)
                .map(m -> {
                    ModuleResponseDto dto = new ModuleResponseDto();
                    dto.setId(m.getId());
                    dto.setCode(m.getCode());
                    dto.setName(m.getName());
                    dto.setRoutePath(m.getRoutePath());
                    dto.setModuleCategory(m.getModuleCategory());
                    dto.setEnabled(m.isEnabled());
                    dto.setSortOrder(m.getSortOrder());
                    return dto;
                })
                .sorted(Comparator.comparing(ModuleResponseDto::getModuleCategory, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ModuleResponseDto::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ModuleResponseDto::getCode))
                .collect(Collectors.toList());

        List<PermissionMatrixDto> permissions = new ArrayList<>();
        for (Long mid : allowedModuleIds) {
            SystemModule m = moduleRepository.findById(mid).orElse(null);
            if (m == null || !m.isEnabled()) continue;
            PermissionMatrixDto dto = new PermissionMatrixDto();
            dto.setModuleId(m.getId());
            dto.setModuleCode(m.getCode());
            dto.setModuleName(m.getName());
            dto.setVisibility(moduleVisibility.getOrDefault(mid, ModuleVisibility.VISIBLE));
            dto.setActions(new HashSet<>(moduleActions.getOrDefault(mid, Collections.emptySet())));
            permissions.add(dto);
        }

        response.setAllowedModules(allowedModules);
        response.setPermissions(permissions);
        return response;
    }

    @Transactional
    public void assignPermissions(PermissionAssignRequestDto request) {
        SystemRole role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + request.getRoleId()));
        SystemModule module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + request.getModuleId()));

        permissionRepository.deleteByRoleIdAndModuleId(role.getId(), module.getId());

        for (ActionType action : request.getActions()) {
            RoleModulePermission rmp = new RoleModulePermission();
            rmp.setRole(role);
            rmp.setModule(module);
            rmp.setActionType(action);
            rmp.setVisibility(request.getVisibility());
            permissionRepository.save(rmp);
        }
    }

    @Transactional(readOnly = true)
    public List<PermissionMatrixDto> getPermissionsForRole(Long roleId) {
        List<RoleModulePermission> list = permissionRepository.findByRoleIdWithModule(roleId);
        Map<Long, PermissionMatrixDto> byModule = new LinkedHashMap<>();
        for (RoleModulePermission rmp : list) {
            Long mid = rmp.getModule().getId();
            PermissionMatrixDto dto = byModule.computeIfAbsent(mid, k -> {
                PermissionMatrixDto p = new PermissionMatrixDto();
                p.setModuleId(rmp.getModule().getId());
                p.setModuleCode(rmp.getModule().getCode());
                p.setModuleName(rmp.getModule().getName());
                p.setVisibility(rmp.getVisibility());
                p.setActions(new HashSet<>());
                return p;
            });
            dto.getActions().add(rmp.getActionType());
            if (rmp.getVisibility() != null) dto.setVisibility(rmp.getVisibility());
        }
        return new ArrayList<>(byModule.values());
    }
}
