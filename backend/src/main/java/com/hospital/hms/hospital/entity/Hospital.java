package com.hospital.hms.hospital.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Hospital / branch master. Multi-hospital ready. DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "hospitals",
    indexes = {
        @Index(name = "idx_hospital_code", columnList = "hospital_code", unique = true),
        @Index(name = "idx_hospital_active", columnList = "is_active")
    }
)
public class Hospital extends BaseIdEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "hospital_code", nullable = false, unique = true, length = 50)
    private String hospitalCode;

    @NotBlank
    @Size(max = 255)
    @Column(name = "hospital_name", nullable = false, length = 255)
    private String hospitalName;

    @Size(max = 500)
    @Column(name = "location", length = 500)
    private String location;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    public Hospital() {
    }

    public String getHospitalCode() {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode) {
        this.hospitalCode = hospitalCode;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
