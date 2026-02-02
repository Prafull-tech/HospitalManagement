package com.hospital.hms.nursing.entity;

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
 * Nursing staff. DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "nursing_staff",
    indexes = {
        @Index(name = "idx_nursing_staff_code", columnList = "code", unique = true),
        @Index(name = "idx_nursing_staff_role", columnList = "nurse_role"),
        @Index(name = "idx_nursing_staff_active", columnList = "is_active")
    }
)
public class NursingStaff extends BaseIdEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank
    @Size(max = 255)
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "nurse_role", nullable = false, length = 40)
    private NurseRole nurseRole;

    @Size(max = 30)
    @Column(name = "phone", length = 30)
    private String phone;

    @Size(max = 255)
    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public NursingStaff() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public NurseRole getNurseRole() {
        return nurseRole;
    }

    public void setNurseRole(NurseRole nurseRole) {
        this.nurseRole = nurseRole;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
