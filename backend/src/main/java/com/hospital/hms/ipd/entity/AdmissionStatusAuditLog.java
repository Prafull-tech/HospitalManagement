package com.hospital.hms.ipd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Audit log for IPD admission status changes. Immutable after insert.
 * Every status transition is recorded: from_status, to_status, changed_by, reason.
 * DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "admission_status_audit_log",
    indexes = {
        @Index(name = "idx_status_audit_admission", columnList = "admission_id"),
        @Index(name = "idx_status_audit_changed_at", columnList = "changed_at"),
        @Index(name = "idx_status_audit_to_status", columnList = "to_status")
    }
)
public class AdmissionStatusAuditLog extends BaseIdEntity {

    @NotNull
    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    /** Previous status; null when admission is first created (→ ADMITTED). */
    @Column(name = "from_status", length = 30)
    @Enumerated(EnumType.STRING)
    private AdmissionStatus fromStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private AdmissionStatus toStatus;

    @NotNull
    @Column(name = "changed_at", nullable = false)
    private java.time.Instant changedAt;

    @Size(max = 255)
    @Column(name = "changed_by", length = 255)
    private String changedBy;

    @Size(max = 500)
    @Column(name = "reason", length = 500)
    private String reason;

    public AdmissionStatusAuditLog() {
    }

    public Long getAdmissionId() {
        return admissionId;
    }

    public void setAdmissionId(Long admissionId) {
        this.admissionId = admissionId;
    }

    public AdmissionStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(AdmissionStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public AdmissionStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(AdmissionStatus toStatus) {
        this.toStatus = toStatus;
    }

    public java.time.Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(java.time.Instant changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
