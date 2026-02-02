package com.hospital.hms.doctor.dto;

import com.hospital.hms.doctor.entity.DoctorStatus;
import com.hospital.hms.doctor.entity.DoctorType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for doctor (view/list).
 */
public class DoctorResponseDto {

    private Long id;
    private String code;
    private String fullName;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private String specialization;
    private DoctorType doctorType;
    private DoctorStatus status;
    private String phone;
    private String email;
    private String qualifications;
    private Boolean onCall;
    private Instant createdAt;
    private Instant updatedAt;
    private List<DoctorAvailabilityResponseDto> availability = new ArrayList<>();

    public DoctorResponseDto() {
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
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

    public List<DoctorAvailabilityResponseDto> getAvailability() {
        return availability;
    }

    public void setAvailability(List<DoctorAvailabilityResponseDto> availability) {
        this.availability = availability != null ? availability : new ArrayList<>();
    }
}
