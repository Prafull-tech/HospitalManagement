package com.hospital.hms.superadmin.dto;

public class HospitalModuleResponseDto {
    private String moduleCode;
    private String moduleName;
    private String moduleCategory;
    private Boolean enabled;
    private Boolean inCurrentPlan;

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleCategory() {
        return moduleCategory;
    }

    public void setModuleCategory(String moduleCategory) {
        this.moduleCategory = moduleCategory;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getInCurrentPlan() {
        return inCurrentPlan;
    }

    public void setInCurrentPlan(Boolean inCurrentPlan) {
        this.inCurrentPlan = inCurrentPlan;
    }
}