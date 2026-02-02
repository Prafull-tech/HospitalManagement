package com.hospital.hms.doctor.dto;

import com.hospital.hms.doctor.entity.DoctorStatus;
import com.hospital.hms.doctor.entity.DoctorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for add/update doctor.
 */
public class DoctorRequestDto {

    @NotBlank(message = "Doctor code is required")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "Full name is required")
    @Size(max = 255)
    private String fullName;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @Size(max = 255)
    private String specialization;

    @NotNull(message = "Doctor type is required")
    private DoctorType doctorType;

    private DoctorStatus status = DoctorStatus.ACTIVE;

    @Size(max = 30)
    private String phone;

    @Size(max = 255)
    private String email;

    @Size(max = 500)
    private String qualifications;

    private Boolean onCall = false;

    public DoctorRequestDto() {
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public DoctorType getDoctorType() {
        return doctorType;
    }

    public void setDoctorType(DoctorType doctorType) {
        this.doctorType = doctorType;
    }

    public DoctorStatus getStatus() {
        return status;
    }

    public void setStatus(DoctorStatus status) {
        this.status = status;
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

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public Boolean getOnCall() {
        return onCall;
    }

    public void setOnCall(Boolean onCall) {
        this.onCall = onCall;
    }
}
