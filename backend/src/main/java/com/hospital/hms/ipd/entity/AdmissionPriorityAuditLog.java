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
 * Audit log for admission priority decisions. Immutable; read-only after insert.
 * Tracks: priority assigned, rule applied, special consideration, override details, approved by, timestamp.
 * DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "admission_priority_audit_log",
    indexes = {
        @Index(name = "idx_priority_audit_admission", columnList = "admission_id"),
        @Index(name = "idx_priority_audit_created", columnList = "created_at")
    }
)
public class AdmissionPriorityAuditLog extends BaseIdEntity {

    @NotNull
    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority_assigned", nullable = false, length = 10)
    private PriorityCode priorityAssigned;

    /** Rule/condition applied (e.g. EMERGENCY, ICU, REFERRED, ELECTIVE). Null for override-only entries. */
    @Size(max = 50)
    @Column(name = "rule_applied", length = 50)
    private String ruleApplied;

    /** Special consideration types applied (e.g. SENIOR_CITIZEN, CHILD). Comma-separated or empty. */
    @Size(max = 255)
    @Column(name = "special_consideration_applied", length = 255)
    private String specialConsiderationApplied;

    @Column(name = "is_override", nullable = false)
    private Boolean isOverride = false;

    /** Override reason when isOverride is true. */
    @Size(max = 500)
    @Column(name = "override_details", length = 500)
    private String overrideDetails;

    /** Username of authority who approved override. Null when system-assigned. */
    @Size(max = 255)
    @Column(name = "approved_by", length = 255)
    private String approvedBy;

    public AdmissionPriorityAuditLog() {
    }

    public Long getAdmissionId() {
        return admissionId;
    }

    public void setAdmissionId(Long admissionId) {
        this.admissionId = admissionId;
    }

    public PriorityCode getPriorityAssigned() {
        return priorityAssigned;
    }

    public void setPriorityAssigned(PriorityCode priorityAssigned) {
        this.priorityAssigned = priorityAssigned;
    }

    public String getRuleApplied() {
        return ruleApplied;
    }

    public void setRuleApplied(String ruleApplied) {
        this.ruleApplied = ruleApplied;
    }

    public String getSpecialConsiderationApplied() {
        return specialConsiderationApplied;
    }

    public void setSpecialConsiderationApplied(String specialConsiderationApplied) {
        this.specialConsiderationApplied = specialConsiderationApplied;
    }

    public Boolean getIsOverride() {
        return isOverride;
    }

    public void setIsOverride(Boolean isOverride) {
        this.isOverride = isOverride;
    }

    public String getOverrideDetails() {
        return overrideDetails;
    }

    public void setOverrideDetails(String overrideDetails) {
        this.overrideDetails = overrideDetails;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
}
