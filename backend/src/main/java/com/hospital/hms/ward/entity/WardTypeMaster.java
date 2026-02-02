package com.hospital.hms.ward.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Ward Type Master – configurable ward types (General, Semi Pvt, Private, ICU, Emergency, etc.).
 * Name must be unique. DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "ward_types",
    indexes = {
        @Index(name = "idx_ward_type_name", columnList = "name", unique = true),
        @Index(name = "idx_ward_type_active", columnList = "is_active")
    }
)
public class WardTypeMaster extends BaseIdEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public WardTypeMaster() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
