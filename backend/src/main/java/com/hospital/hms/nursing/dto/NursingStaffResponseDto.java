package com.hospital.hms.nursing.dto;

import com.hospital.hms.nursing.entity.NurseRole;

import java.time.Instant;

/**
 * Response DTO for nursing staff.
 */
public class NursingStaffResponseDto {

    private Long id;
    private String code;
    private String fullName;
    private NurseRole nurseRole;
    private String phone;
    private String email;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public NursingStaffResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
