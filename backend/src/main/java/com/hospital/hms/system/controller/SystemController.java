package com.hospital.hms.system.controller;

import com.hospital.hms.system.dto.*;
import com.hospital.hms.system.service.FeatureToggleService;
import com.hospital.hms.system.service.PermissionService;
import com.hospital.hms.system.service.SystemModuleService;
import com.hospital.hms.system.service.SystemRoleService;
import com.hospital.hms.system.service.CompanyProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * System Configuration / Access Control REST API.
 * Base path: /api (context) + /system (mapping).
 * Reads require authentication; writes require ADMIN. /permissions/me uses JWT roles only (not X-Roles).
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    private final SystemRoleService roleService;
    private final SystemModuleService moduleService;
    private final PermissionService permissionService;
    private final FeatureToggleService featureToggleService;
    private final CompanyProfileService companyProfileService;

    public SystemController(SystemRoleService roleService,
                             SystemModuleService moduleService,
                             PermissionService permissionService,
                             FeatureToggleService featureToggleService,
                             CompanyProfileService companyProfileService) {
        this.roleService = roleService;
        this.moduleService = moduleService;
        this.permissionService = permissionService;
        this.featureToggleService = featureToggleService;
        this.companyProfileService = companyProfileService;
    }

    /** Role codes from JWT (ROLE_* authorities). Ignores client-supplied role headers. */
    static List<String> roleCodesFromAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Collections.emptyList();
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a != null && a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .collect(Collectors.toList());
    }

    // ---------- Roles ----------
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleResponseDto>> listRoles(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(roleService.listAll(activeOnly));
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDto> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getById(id));
    }

    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody RoleRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(request));
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDto> updateRole(@PathVariable Long id,
                                                       @Valid @RequestBody RoleRequestDto request) {
        return ResponseEntity.ok(roleService.update(id, request));
    }

    // ---------- Modules ----------
    @GetMapping("/modules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ModuleResponseDto>> listModules(
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        return ResponseEntity.ok(moduleService.listAll(enabledOnly));
    }

    @GetMapping("/modules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponseDto> getModule(@PathVariable Long id) {
        return ResponseEntity.ok(moduleService.getById(id));
    }

    @PostMapping("/modules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponseDto> createModule(@Valid @RequestBody ModuleRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(moduleService.create(request));
    }

    @PutMapping("/modules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponseDto> updateModule(@PathVariable Long id,
                                                           @Valid @RequestBody ModuleRequestDto request) {
        return ResponseEntity.ok(moduleService.update(id, request));
    }

    // ---------- Permissions ----------
    @PostMapping("/permissions/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignPermissions(@Valid @RequestBody PermissionAssignRequestDto request) {
        permissionService.assignPermissions(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Effective permissions for the authenticated user (roles from JWT only).
     */
    @GetMapping("/permissions/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MyPermissionsResponseDto> getMyPermissions() {
        List<String> roleCodes = roleCodesFromAuthentication();
        return ResponseEntity.ok(permissionService.getMyPermissions(roleCodes));
    }

    @GetMapping("/permissions/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PermissionMatrixDto>> getPermissionsForRole(@PathVariable Long roleId) {
        return ResponseEntity.ok(permissionService.getPermissionsForRole(roleId));
    }

    // ---------- Feature toggles ----------
    @GetMapping("/features")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<FeatureToggleResponseDto>> listFeatures() {
        return ResponseEntity.ok(featureToggleService.listAll());
    }

    /**
     * Newer UI calls /system/features/effective. For now it is equivalent to /system/features.
     * (Effective toggles may be added later when multiple sources exist.)
     */
    @GetMapping("/features/effective")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<FeatureToggleResponseDto>> listEffectiveFeatures() {
        return ResponseEntity.ok(featureToggleService.listAll());
    }

    @PatchMapping("/features/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<FeatureToggleResponseDto> setFeatureEnabled(@PathVariable Long id,
                                                                      @RequestParam boolean enabled) {
        return ResponseEntity.ok(featureToggleService.setEnabled(id, enabled));
    }

    // ---------- Company profile ----------
    @GetMapping("/company-profile")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<CompanyProfileDto> getCompanyProfile() {
        return ResponseEntity.ok(companyProfileService.getProfile());
    }

    @PutMapping("/company-profile")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<CompanyProfileDto> updateCompanyProfile(@Valid @RequestBody CompanyProfileDto request) {
        return ResponseEntity.ok(companyProfileService.updateProfile(request));
    }
}
