package com.hospital.hms.system.dto;

import com.hospital.hms.system.entity.ActionType;
import com.hospital.hms.system.entity.ModuleVisibility;

import java.util.Set;

/**
 * One module's permissions for a role (or effective for current user).
 */
public class PermissionMatrixDto {

    private Long moduleId;
    private String moduleCode;
    private String moduleName;
    private ModuleVisibility visibility;
    private Set<ActionType> actions;

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public ModuleVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ModuleVisibility visibility) {
        this.visibility = visibility;
    }

    public Set<ActionType> getActions() {
        return actions;
    }

    public void setActions(Set<ActionType> actions) {
        this.actions = actions;
    }
}
