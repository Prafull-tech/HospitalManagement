package com.hospital.hms.nursing.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.ipd.entity.IPDAdmission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Nurse assigned to an IPD admission for a shift. DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "nurse_assignments",
    indexes = {
        @Index(name = "idx_nurse_assignment_nurse", columnList = "nursing_staff_id"),
        @Index(name = "idx_nurse_assignment_admission", columnList = "ipd_admission_id"),
        @Index(name = "idx_nurse_assignment_shift_date", columnList = "shift_type, assignment_date")
    }
)
public class NurseAssignment extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nursing_staff_id", nullable = false)
    private NursingStaff nursingStaff;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipd_admission_id", nullable = false)
    private IPDAdmission ipdAdmission;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false, length = 20)
    private ShiftType shiftType;

    @NotNull
    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Size(max = 500)
    @Column(name = "remarks", length = 500)
    private String remarks;

    public NurseAssignment() {
    }

    public NursingStaff getNursingStaff() {
        return nursingStaff;
    }

    public void setNursingStaff(NursingStaff nursingStaff) {
        this.nursingStaff = nursingStaff;
    }

    public IPDAdmission getIpdAdmission() {
        return ipdAdmission;
    }

    public void setIpdAdmission(IPDAdmission ipdAdmission) {
        this.ipdAdmission = ipdAdmission;
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
}
