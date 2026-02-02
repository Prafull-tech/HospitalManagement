package com.hospital.hms.ipd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Family consent record for patient transfers.
 * <ul>
 *   <li>Mandatory for non-emergency transfers</li>
 *   <li>Emergency may allow deferred consent (record created later)</li>
 * </ul>
 * DB-agnostic JPA design (H2 & MySQL).
 */
@Entity
@Table(
    name = "transfer_consent",
    indexes = {
        @Index(name = "idx_transfer_consent_recommendation", columnList = "transfer_recommendation_id"),
        @Index(name = "idx_transfer_consent_time", columnList = "consent_time")
    }
)
public class TransferConsent extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_recommendation_id", nullable = false)
    private TransferRecommendation transferRecommendation;

    @NotNull
    @Column(name = "consent_given", nullable = false)
    private Boolean consentGiven;

    @Size(max = 255)
    @Column(name = "consent_by_name", length = 255)
    private String consentByName;

    @Size(max = 100)
    @Column(name = "relation_to_patient", length = 100)
    private String relationToPatient;

    @Column(name = "consent_time")
    private Instant consentTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "consent_mode", nullable = false, length = 20)
    private ConsentMode consentMode;

    public TransferConsent() {
    }

    public TransferRecommendation getTransferRecommendation() {
        return transferRecommendation;
    }

    public void setTransferRecommendation(TransferRecommendation transferRecommendation) {
        this.transferRecommendation = transferRecommendation;
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
