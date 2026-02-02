package com.hospital.hms.system.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Permission: Role × Module × Action. One row per (role, module, action).
 * DB-agnostic (H2 & MySQL). Indexed for fast lookup by role and module.
 */
@Entity
@Table(
    name = "role_module_permissions",
    indexes = {
        @Index(name = "idx_rmp_role", columnList = "role_id"),
        @Index(name = "idx_rmp_module", columnList = "module_id"),
        @Index(name = "idx_rmp_role_module_action", columnList = "role_id, module_id, action_type", unique = true)
    }
)
public class RoleModulePermission extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private SystemRole role;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private SystemModule module;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private ActionType actionType;

    /** Optional: visibility override for this role-module (default from module). */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 20)
    private ModuleVisibility visibility;

    public RoleModulePermission() {
    }

    public SystemRole getRole() {
        return role;
    }

    public void setRole(SystemRole role) {
        this.role = role;
    }

    public SystemModule getModule() {
        return module;
    }

    public void setModule(SystemModule module) {
        this.module = module;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public ModuleVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ModuleVisibility visibility) {
        this.visibility = visibility;
    }
}
