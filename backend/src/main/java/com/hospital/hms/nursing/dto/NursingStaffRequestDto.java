package com.hospital.hms.nursing.dto;

import com.hospital.hms.nursing.entity.NurseRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating/updating nursing staff.
 */
public class NursingStaffRequestDto {

    @NotBlank(message = "Code is required")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "Full name is required")
    @Size(max = 255)
    private String fullName;

    @NotNull(message = "Nurse role is required")
    private NurseRole nurseRole;

    @Size(max = 30)
    private String phone;

    @Size(max = 255)
    private String email;

    private Boolean isActive = true;

    public NursingStaffRequestDto() {
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
