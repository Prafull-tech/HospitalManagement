package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.ConsentMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for POST /api/ipd/transfers/consent.
 */
public class TransferConsentRequestDto {

    @NotNull(message = "Transfer recommendation ID is required")
    private Long transferRecommendationId;

    @NotNull(message = "Consent given flag is required")
    private Boolean consentGiven;

    @Size(max = 255)
    private String consentByName;

    @Size(max = 100)
    private String relationToPatient;

    @NotNull(message = "Consent mode is required")
    private ConsentMode consentMode;

    public TransferConsentRequestDto() {
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

    public ConsentMode getConsentMode() {
        return consentMode;
    }

    public void setConsentMode(ConsentMode consentMode) {
        this.consentMode = consentMode;
    }
}
