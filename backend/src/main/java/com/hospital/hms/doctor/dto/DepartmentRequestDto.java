package com.hospital.hms.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for add/update department.
 */
public class DepartmentRequestDto {

    @NotBlank(message = "Department code is required")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "Department name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 500)
    private String description;

    private Long hodDoctorId;

    public DepartmentRequestDto() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getHodDoctorId() {
        return hodDoctorId;
    }

    public void setHodDoctorId(Long hodDoctorId) {
        this.hodDoctorId = hodDoctorId;
    }
}
