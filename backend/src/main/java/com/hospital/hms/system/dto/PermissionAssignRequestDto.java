package com.hospital.hms.system.dto;

import com.hospital.hms.system.entity.ActionType;
import com.hospital.hms.system.entity.ModuleVisibility;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class PermissionAssignRequestDto {

    @NotNull
    private Long roleId;

    @NotNull
    private Long moduleId;

    @NotNull
    @Size(min = 0)
    private Set<ActionType> actions;

    private ModuleVisibility visibility;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public Set<ActionType> getActions() {
        return actions;
    }

    public void setActions(Set<ActionType> actions) {
        this.actions = actions;
    }

    public ModuleVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ModuleVisibility visibility) {
        this.visibility = visibility;
    }
}
