package com.hospital.hms.doctor.dto;

/**
 * Response DTO for department (list/dropdown).
 */
public class DepartmentResponseDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Long hodDoctorId;
    private String hodDoctorName;

    public DepartmentResponseDto() {
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

    public String getHodDoctorName() {
        return hodDoctorName;
    }

    public void setHodDoctorName(String hodDoctorName) {
        this.hodDoctorName = hodDoctorName;
    }
}
