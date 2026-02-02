package com.hospital.hms.ipd.dto;

import java.time.Instant;

/**
 * Response DTO for transfer audit log (read-only compliance API). DB-agnostic.
 */
public class TransferAuditResponseDto {

    private Long id;
    private Long transferRecommendationId;
    private Long ipdAdmissionId;
    private String action;
    private String performedBy;
    private String performedByRole;
    private Long recommendedByDoctorId;
    private Boolean consentGiven;
    private String consentByName;
    private String consentMode;
    private Boolean emergencyFlag;
    private String fromWardType;
    private String toWardType;
    private Long newBedId;
    private String details;
    private Instant timestamp;

    public TransferAuditResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
