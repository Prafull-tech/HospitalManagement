package com.hospital.hms.hospital.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Audit log for bed availability changes. One row per update; read-only history.
 * Records who changed, when, and which role/action (Nurse update, IPD Desk verify, Ward In-charge approve).
 * System logs timestamp and user on every change.
 */
@Entity
@Table(
    name = "bed_availability_audit_log",
    indexes = {
        @Index(name = "idx_bed_avail_audit_record", columnList = "bed_availability_id"),
        @Index(name = "idx_bed_avail_audit_changed_at", columnList = "bed_availability_id, changed_at")
    }
)
public class BedAvailabilityAuditLog extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_availability_id", nullable = false, updatable = false)
    private BedAvailability bedAvailability;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    @Column(name = "changed_by", nullable = false, length = 255, updatable = false)
    private String changedBy;

    /** Role that performed the action (e.g. NURSE, IPD_DESK, WARD_INCHARGE, ADMIN, IPD_MANAGER). */
    @Column(name = "performed_by_role", length = 50, updatable = false)
    private String performedByRole;

    /** Action performed (e.g. UPDATE, VERIFY, APPROVE, CREATE). */
    @Column(name = "action", length = 50, updatable = false)
    private String action;

    public BedAvailabilityAuditLog() {
    }

    public BedAvailability getBedAvailability() {
        return bedAvailability;
    }

    public void setBedAvailability(BedAvailability bedAvailability) {
        this.bedAvailability = bedAvailability;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getPerformedByRole() {
        return performedByRole;
    }

    public void setPerformedByRole(String performedByRole) {
        this.performedByRole = performedByRole;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
