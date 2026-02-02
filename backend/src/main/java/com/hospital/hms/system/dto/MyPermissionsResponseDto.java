package com.hospital.hms.system.dto;

import java.util.List;

/**
 * Effective permissions and modules for the current user (for sidebar and route guard).
 */
public class MyPermissionsResponseDto {

    private List<String> roleCodes;
    private List<ModuleResponseDto> allowedModules;
    private List<PermissionMatrixDto> permissions;

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }

    public List<ModuleResponseDto> getAllowedModules() {
        return allowedModules;
    }

    public void setAllowedModules(List<ModuleResponseDto> allowedModules) {
        this.allowedModules = allowedModules;
    }

    public List<PermissionMatrixDto> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionMatrixDto> permissions) {
        this.permissions = permissions;
    }
}
