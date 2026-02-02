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
 * Audit log for transfers. Tracks who recommended, who approved, consent details,
 * emergency flag, old ward → new ward, timestamp. Read-only after insert. DB-agnostic.
 */
@Entity
@Table(
    name = "transfer_audit_log",
    indexes = {
        @Index(name = "idx_transfer_audit_recommendation", columnList = "transfer_recommendation_id"),
        @Index(name = "idx_transfer_audit_admission", columnList = "ipd_admission_id"),
        @Index(name = "idx_transfer_audit_created", columnList = "created_at")
    }
)
public class TransferAuditLog extends BaseIdEntity {

    @Column(name = "transfer_recommendation_id")
    private Long transferRecommendationId;

    @NotNull
    @Column(name = "ipd_admission_id", nullable = false)
    private Long ipdAdmissionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private TransferAuditAction action;

    /** Who performed the action (e.g. username or staff identifier). */
    @Size(max = 255)
    @Column(name = "performed_by", length = 255)
    private String performedBy;

    @Size(max = 50)
    @Column(name = "performed_by_role", length = 50)
    private String performedByRole;

    /** Who recommended (doctor ID); for RECOMMENDED action. */
    @Column(name = "recommended_by_doctor_id")
    private Long recommendedByDoctorId;

    /** Consent details: given or not. */
    @Column(name = "consent_given")
    private Boolean consentGiven;

    @Size(max = 255)
    @Column(name = "consent_by_name", length = 255)
    private String consentByName;

    @Size(max = 20)
    @Column(name = "consent_mode", length = 20)
    private String consentMode;

    @Column(name = "emergency_flag")
    private Boolean emergencyFlag;

    @Size(max = 30)
    @Column(name = "from_ward_type", length = 30)
    private String fromWardType;

    @Size(max = 30)
    @Column(name = "to_ward_type", length = 30)
    private String toWardType;

    @Column(name = "new_bed_id")
    private Long newBedId;

    @Size(max = 500)
    @Column(name = "details", length = 500)
    private String details;

    public TransferAuditLog() {
    }

    public Long getTransferRecommendationId() {
        return transferRecommendationId;
    }

    public void setTransferRecommendationId(Long transferRecommendationId) {
        this.transferRecommendationId = transferRecommendationId;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }

    public TransferAuditAction getAction() {
        return action;
    }

    public void setAction(TransferAuditAction action) {
        this.action = action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getPerformedByRole() {
        return performedByRole;
    }

    public void setPerformedByRole(String performedByRole) {
        this.performedByRole = performedByRole;
    }

    public Long getRecommendedByDoctorId() {
        return recommendedByDoctorId;
    }

    public void setRecommendedByDoctorId(Long recommendedByDoctorId) {
        this.recommendedByDoctorId = recommendedByDoctorId;
    }

    public Boolean getConsentGiven() {
        return consentGiven;
    }

    public void setConsentGiven(Boolean consentGiven) {
        this.consentGiven = consentGiven;
    }

    public String getConsentByName() {
        return consentByName;
    }

    public void setConsentByName(String consentByName) {
        this.consentByName = consentByName;
    }

    public String getConsentMode() {
        return consentMode;
    }

    public void setConsentMode(String consentMode) {
        this.consentMode = consentMode;
    }

    public Boolean getEmergencyFlag() {
        return emergencyFlag;
    }

    public void setEmergencyFlag(Boolean emergencyFlag) {
        this.emergencyFlag = emergencyFlag;
    }

    public String getFromWardType() {
        return fromWardType;
    }

    public void setFromWardType(String fromWardType) {
        this.fromWardType = fromWardType;
    }

    public String getToWardType() {
        return toWardType;
    }

    public void setToWardType(String toWardType) {
        this.toWardType = toWardType;
    }

    public Long getNewBedId() {
        return newBedId;
    }

    public void setNewBedId(Long newBedId) {
        this.newBedId = newBedId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
