package com.hospital.hms.nursing.dto;

import com.hospital.hms.nursing.entity.ShiftType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for nurse assignment.
 */
public class NurseAssignmentResponseDto {

    private Long id;
    private Long nursingStaffId;
    private String nursingStaffName;
    private String nursingStaffCode;
    private Long ipdAdmissionId;
    private String admissionNumber;
    private ShiftType shiftType;
    private LocalDate assignmentDate;
    private LocalDateTime assignedAt;
    private String remarks;
    private Instant createdAt;

    public NurseAssignmentResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNursingStaffId() {
        return nursingStaffId;
    }

    public void setNursingStaffId(Long nursingStaffId) {
        this.nursingStaffId = nursingStaffId;
    }

    public String getNursingStaffName() {
        return nursingStaffName;
    }

    public void setNursingStaffName(String nursingStaffName) {
        this.nursingStaffName = nursingStaffName;
    }

    public String getNursingStaffCode() {
        return nursingStaffCode;
    }

    public void setNursingStaffCode(String nursingStaffCode) {
        this.nursingStaffCode = nursingStaffCode;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }

    public String getAdmissionNumber() {
        return admissionNumber;
    }

    public void setAdmissionNumber(String admissionNumber) {
        this.admissionNumber = admissionNumber;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public LocalDate getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(LocalDate assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
