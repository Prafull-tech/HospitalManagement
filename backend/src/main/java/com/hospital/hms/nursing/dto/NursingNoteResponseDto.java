package com.hospital.hms.nursing.dto;

import com.hospital.hms.nursing.entity.NoteStatus;
import com.hospital.hms.nursing.entity.ShiftType;
import com.hospital.hms.ward.entity.WardType;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Response DTO for nursing note.
 */
public class NursingNoteResponseDto {

    private Long id;
    private Long ipdAdmissionId;
    private String admissionNumber;
    private ShiftType shiftType;
    private String noteType;
    private String content;
    private LocalDateTime recordedAt;
    private Long recordedById;
    private String recordedByName;
    private NoteStatus noteStatus;
    private LocalDateTime lockedAt;
    private Long lockedById;
    private String lockedByName;
    private String criticalFlags;
    private WardType wardType;
    private String wardName;
    private String bedNumber;
    private String patientName;
    private String patientUhid;
    private Instant createdAt;
    private Instant updatedAt;

    public NursingNoteResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
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

    public Long getRecordedById() {
        return recordedById;
    }

    public void setRecordedById(Long recordedById) {
        this.recordedById = recordedById;
    }

    public String getRecordedByName() {
        return recordedByName;
    }

    public void setRecordedByName(String recordedByName) {
        this.recordedByName = recordedByName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }
    public ShiftType getShiftType() { return shiftType; }
    public void setShiftType(ShiftType shiftType) { this.shiftType = shiftType; }
    public NoteStatus getNoteStatus() { return noteStatus; }
    public void setNoteStatus(NoteStatus noteStatus) { this.noteStatus = noteStatus; }
    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }
    public Long getLockedById() { return lockedById; }
    public void setLockedById(Long lockedById) { this.lockedById = lockedById; }
    public String getLockedByName() { return lockedByName; }
    public void setLockedByName(String lockedByName) { this.lockedByName = lockedByName; }
    public String getCriticalFlags() { return criticalFlags; }
    public void setCriticalFlags(String criticalFlags) { this.criticalFlags = criticalFlags; }
    public WardType getWardType() { return wardType; }
    public void setWardType(WardType wardType) { this.wardType = wardType; }
    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }
    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getPatientUhid() { return patientUhid; }
    public void setPatientUhid(String patientUhid) { this.patientUhid = patientUhid; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
