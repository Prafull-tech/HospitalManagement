package com.hospital.hms.nursing.dto;

import com.hospital.hms.nursing.entity.ShiftType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request DTO for nursing note (create/update).
 */
public class NursingNoteRequestDto {

    @NotNull(message = "IPD admission ID is required")
    private Long ipdAdmissionId;

    @NotNull(message = "Shift type is required")
    private ShiftType shiftType;

    @NotBlank(message = "Note type is required")
    @Size(max = 50)
    private String noteType;

    @NotBlank(message = "Content is required")
    private String content;

    private LocalDateTime recordedAt;

    private Long recordedById;

    @Size(max = 100)
    private String criticalFlags;

    public NursingNoteRequestDto() {
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

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public String getCriticalFlags() {
        return criticalFlags;
    }

    public void setCriticalFlags(String criticalFlags) {
        this.criticalFlags = criticalFlags;
    }
}
