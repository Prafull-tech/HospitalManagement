package com.hospital.hms.superadmin.dto;

import jakarta.validation.constraints.NotBlank;

public class HospitalModuleItemRequestDto {

    @NotBlank(message = "Module code is required")
    private String moduleCode;

    private Boolean enabled;

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}