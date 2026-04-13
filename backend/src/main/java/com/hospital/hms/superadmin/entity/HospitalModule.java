package com.hospital.hms.superadmin.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.hospital.entity.Hospital;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
    name = "hospital_modules",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_hospital_module_code", columnNames = {"hospital_id", "module_code"})
    },
    indexes = {
        @Index(name = "idx_hospital_module_hospital", columnList = "hospital_id"),
        @Index(name = "idx_hospital_module_code", columnList = "module_code")
    }
)
public class HospitalModule extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotBlank
    @Size(max = 50)
    @Column(name = "module_code", nullable = false, length = 50)
    private String moduleCode;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}