package com.hospital.hms.system.controller;

import com.hospital.hms.system.dto.*;
import com.hospital.hms.system.service.FeatureToggleService;
import com.hospital.hms.system.service.PermissionService;
import com.hospital.hms.system.service.SystemModuleService;
import com.hospital.hms.system.service.SystemRoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * System Configuration / Access Control REST API.
 * Base path: /api (context) + /system (mapping).
 * When auth is enabled, restrict to ADMIN/SUPER_ADMIN for write; /permissions/me for any authenticated user.
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    private final SystemRoleService roleService;
    private final SystemModuleService moduleService;
    private final PermissionService permissionService;
    private final FeatureToggleService featureToggleService;

    public SystemController(SystemRoleService roleService,
                             SystemModuleService moduleService,
                             PermissionService permissionService,
                             FeatureToggleService featureToggleService) {
        this.roleService = roleService;
        this.moduleService = moduleService;
        this.permissionService = permissionService;
        this.featureToggleService = featureToggleService;
    }

    // ---------- Roles ----------
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponseDto>> listRoles(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(roleService.listAll(activeOnly));
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<RoleResponseDto> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getById(id));
    }

    @PostMapping("/roles")
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody RoleRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(request));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<RoleResponseDto> updateRole(@PathVariable Long id,
                                                       @Valid @RequestBody RoleRequestDto request) {
        return ResponseEntity.ok(roleService.update(id, request));
    }

    // ---------- Modules ----------
    @GetMapping("/modules")
    public ResponseEntity<List<ModuleResponseDto>> listModules(
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        return ResponseEntity.ok(moduleService.listAll(enabledOnly));
    }

    @GetMapping("/modules/{id}")
    public ResponseEntity<ModuleResponseDto> getModule(@PathVariable Long id) {
        return ResponseEntity.ok(moduleService.getById(id));
    }

    @PostMapping("/modules")
    public ResponseEntity<ModuleResponseDto> createModule(@Valid @RequestBody ModuleRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(moduleService.create(request));
    }

    @PutMapping("/modules/{id}")
    public ResponseEntity<ModuleResponseDto> updateModule(@PathVariable Long id,
                                                           @Valid @RequestBody ModuleRequestDto request) {
        return ResponseEntity.ok(moduleService.update(id, request));
    }

    // ---------- Permissions ----------
    @PostMapping("/permissions/assign")
    public ResponseEntity<Void> assignPermissions(@Valid @RequestBody PermissionAssignRequestDto request) {
        permissionService.assignPermissions(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Effective permissions and allowed modules for the current user.
     * When auth is disabled: pass role codes via header X-Roles (comma-separated, e.g. ADMIN,RECEPTIONIST).
     * When auth is enabled: read from SecurityContextHolder.getAuthentication().getAuthorities().
     */
    @GetMapping("/permissions/me")
    public ResponseEntity<MyPermissionsResponseDto> getMyPermissions(
            @RequestHeader(value = "X-Roles", required = false) String rolesHeader) {
        List<String> roleCodes = parseRoleCodes(rolesHeader);
        return ResponseEntity.ok(permissionService.getMyPermissions(roleCodes));
    }

    @GetMapping("/permissions/role/{roleId}")
    public ResponseEntity<List<PermissionMatrixDto>> getPermissionsForRole(@PathVariable Long roleId) {
        return ResponseEntity.ok(permissionService.getPermissionsForRole(roleId));
    }

    // ---------- Feature toggles ----------
    @GetMapping("/features")
    public ResponseEntity<List<FeatureToggleResponseDto>> listFeatures() {
        return ResponseEntity.ok(featureToggleService.listAll());
    }

    @PatchMapping("/features/{id}")
    public ResponseEntity<FeatureToggleResponseDto> setFeatureEnabled(@PathVariable Long id,
                                                                      @RequestParam boolean enabled) {
        return ResponseEntity.ok(featureToggleService.setEnabled(id, enabled));
    }

    private List<String> parseRoleCodes(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
