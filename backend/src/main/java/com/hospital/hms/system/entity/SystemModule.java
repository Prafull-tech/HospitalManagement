package com.hospital.hms.system.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * System module (e.g. Reception, OPD, IPD). Visibility and enabled state control access.
 * DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "system_modules",
    indexes = {
        @Index(name = "idx_system_module_code", columnList = "code", unique = true),
        @Index(name = "idx_system_module_category", columnList = "module_category"),
        @Index(name = "idx_system_module_enabled", columnList = "enabled")
    }
)
public class SystemModule extends BaseIdEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "module_category", nullable = false, length = 30)
    private ModuleCategory moduleCategory = ModuleCategory.CLINICAL;

    /** Route path for frontend (e.g. /reception, /opd). */
    @Size(max = 100)
    @Column(name = "route_path", length = 100)
    private String routePath;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public SystemModule() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ModuleCategory getModuleCategory() {
        return moduleCategory;
    }

    public void setModuleCategory(ModuleCategory moduleCategory) {
        this.moduleCategory = moduleCategory;
    }

    public String getRoutePath() {
        return routePath;
    }

    public void setRoutePath(String routePath) {
        this.routePath = routePath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
