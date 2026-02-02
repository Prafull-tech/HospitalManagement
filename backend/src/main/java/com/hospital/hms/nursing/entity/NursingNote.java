package com.hospital.hms.nursing.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ward.entity.WardType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Nursing note (shift-wise clinical notes for admitted patients).
 * Supports ward-specific workflows, locking, and audit. DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "nursing_notes",
    indexes = {
        @Index(name = "idx_nursing_note_admission", columnList = "ipd_admission_id"),
        @Index(name = "idx_nursing_note_recorded_at", columnList = "recorded_at"),
        @Index(name = "idx_nursing_note_shift", columnList = "shift_type"),
        @Index(name = "idx_nursing_note_status", columnList = "note_status"),
        @Index(name = "idx_nursing_note_ward", columnList = "ward_type"),
        @Index(name = "idx_nursing_note_patient_uhid", columnList = "patient_uhid"),
        @Index(name = "idx_nursing_note_recorded_date", columnList = "recorded_date")
    }
)
public class NursingNote extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipd_admission_id", nullable = false)
    private IPDAdmission ipdAdmission;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false, length = 20)
    private ShiftType shiftType;

    @NotBlank
    @Size(max = 50)
    @Column(name = "note_type", nullable = false, length = 50)
    private String noteType;

    @NotBlank
    @Column(name = "content", nullable = false, columnDefinition = "CLOB")
    private String content;

    @NotNull
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "recorded_date", nullable = false)
    private java.time.LocalDate recordedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id")
    private NursingStaff recordedBy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "note_status", nullable = false, length = 20)
    private NoteStatus noteStatus = NoteStatus.DRAFT;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locked_by_id")
    private NursingStaff lockedBy;

    @Size(max = 100)
    @Column(name = "critical_flags", length = 100)
    private String criticalFlags;

    @Enumerated(EnumType.STRING)
    @Column(name = "ward_type", length = 30)
    private WardType wardType;

    @Size(max = 100)
    @Column(name = "ward_name", length = 100)
    private String wardName;

    @Size(max = 30)
    @Column(name = "bed_number", length = 30)
    private String bedNumber;

    @Size(max = 255)
    @Column(name = "patient_name", length = 255)
    private String patientName;

    @Size(max = 50)
    @Column(name = "patient_uhid", length = 50)
    private String patientUhid;

    public NursingNote() {
    }

    public IPDAdmission getIpdAdmission() {
        return ipdAdmission;
    }

    public void setIpdAdmission(IPDAdmission ipdAdmission) {
        this.ipdAdmission = ipdAdmission;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public NursingStaff getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(NursingStaff recordedBy) {
        this.recordedBy = recordedBy;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public java.time.LocalDate getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(java.time.LocalDate recordedDate) {
        this.recordedDate = recordedDate;
    }

    public NoteStatus getNoteStatus() {
        return noteStatus;
    }

    public void setNoteStatus(NoteStatus noteStatus) {
        this.noteStatus = noteStatus;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public NursingStaff getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(NursingStaff lockedBy) {
        this.lockedBy = lockedBy;
    }

    public String getCriticalFlags() {
        return criticalFlags;
    }

    public void setCriticalFlags(String criticalFlags) {
        this.criticalFlags = criticalFlags;
    }

    public WardType getWardType() {
        return wardType;
    }

    public void setWardType(WardType wardType) {
        this.wardType = wardType;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientUhid() {
        return patientUhid;
    }

    public void setPatientUhid(String patientUhid) {
        this.patientUhid = patientUhid;
    }
}
