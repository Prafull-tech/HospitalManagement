package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.ConsentMode;

import java.time.Instant;

/**
 * Response DTO for transfer consent API.
 */
public class TransferConsentResponseDto {

    private Long id;
    private Long transferRecommendationId;
    private Boolean consentGiven;
    private String consentByName;
    private String relationToPatient;
    private Instant consentTime;
    private ConsentMode consentMode;

    public TransferConsentResponseDto() {
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

    public String getRelationToPatient() {
        return relationToPatient;
    }

    public void setRelationToPatient(String relationToPatient) {
        this.relationToPatient = relationToPatient;
    }

    public Instant getConsentTime() {
        return consentTime;
    }

    public void setConsentTime(Instant consentTime) {
        this.consentTime = consentTime;
    }

    public ConsentMode getConsentMode() {
        return consentMode;
    }

    public void setConsentMode(ConsentMode consentMode) {
        this.consentMode = consentMode;
    }
}
