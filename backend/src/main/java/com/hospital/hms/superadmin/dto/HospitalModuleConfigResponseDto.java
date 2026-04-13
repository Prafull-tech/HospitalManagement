package com.hospital.hms.superadmin.dto;

import java.util.ArrayList;
import java.util.List;

public class HospitalModuleConfigResponseDto {
    private Long hospitalId;
    private Boolean hasActivePlan;
    private Long planId;
    private String planCode;
    private String planName;
    private List<HospitalModuleResponseDto> modules = new ArrayList<>();

    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }

    public Boolean getHasActivePlan() {
        return hasActivePlan;
    }

    public void setHasActivePlan(Boolean hasActivePlan) {
        this.hasActivePlan = hasActivePlan;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public List<HospitalModuleResponseDto> getModules() {
        return modules;
    }

    public void setModules(List<HospitalModuleResponseDto> modules) {
        this.modules = modules;
    }
}