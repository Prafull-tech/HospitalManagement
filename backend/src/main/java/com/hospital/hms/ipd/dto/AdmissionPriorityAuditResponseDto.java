package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.PriorityCode;

import java.time.Instant;

/**
 * Response DTO for admission priority audit log (read-only API). DB-agnostic.
 */
public class AdmissionPriorityAuditResponseDto {

    private Long id;
    private Long admissionId;
    private PriorityCode priorityAssigned;
    private String ruleApplied;
    private String specialConsiderationApplied;
    private Boolean isOverride;
    private String overrideDetails;
    private String approvedBy;
    private Instant timestamp;

    public AdmissionPriorityAuditResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
