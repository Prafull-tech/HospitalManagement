package com.hospital.hms.nursing.dto;

import com.hospital.hms.nursing.entity.ShiftType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request DTO for assigning a nurse to an IPD admission.
 */
public class NurseAssignmentRequestDto {

    @NotNull(message = "Nursing staff ID is required")
    private Long nursingStaffId;

    @NotNull(message = "IPD admission ID is required")
    private Long ipdAdmissionId;

    @NotNull(message = "Shift type is required")
    private ShiftType shiftType;

    @NotNull(message = "Assignment date is required")
    private LocalDate assignmentDate;

    @Size(max = 500)
    private String remarks;

    public NurseAssignmentRequestDto() {
    }

    public Long getNursingStaffId() {
        return nursingStaffId;
    }

    public void setNursingStaffId(Long nursingStaffId) {
        this.nursingStaffId = nursingStaffId;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
