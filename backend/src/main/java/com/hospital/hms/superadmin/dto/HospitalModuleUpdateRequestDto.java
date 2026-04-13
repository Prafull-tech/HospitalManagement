package com.hospital.hms.superadmin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HospitalModuleUpdateRequestDto {

    @Valid
    @NotNull(message = "Modules are required")
    private List<HospitalModuleItemRequestDto> modules = new ArrayList<>();

    public List<HospitalModuleItemRequestDto> getModules() {
        return modules;
    }

    public void setModules(List<HospitalModuleItemRequestDto> modules) {
        this.modules = modules;
    }
}