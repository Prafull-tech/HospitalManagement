package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.AdmissionStatus;

import java.time.Instant;

/**
 * Response for POST /api/ipd/transfers: full workflow result.
 * Includes admission summary, step IDs, and system update summary (old bed → VACANT, new bed → OCCUPIED, admission → SHIFTED).
 */
public class IPDTransferFullResponseDto {

    private Long ipdAdmissionId;
    private String admissionNumber;
    private AdmissionStatus admissionStatus;
    private Long currentBedId;
    private String currentBedNumber;
    private Long currentWardId;
    private String currentWardName;

    private Long recommendationId;
    private Long consentId;
    private Long bedReservationId;
    private Long patientTransferId;

    private String oldBedStatus = "VACANT";
    private String newBedStatus = "OCCUPIED";
    private String systemUpdateSummary;
    private Instant transferredAt;

    public IPDTransferFullResponseDto() {
    }

    public Long getIpdAdmissionId() { return ipdAdmissionId; }
    public void setIpdAdmissionId(Long ipdAdmissionId) { this.ipdAdmissionId = ipdAdmissionId; }
    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }
    public AdmissionStatus getAdmissionStatus() { return admissionStatus; }
    public void setAdmissionStatus(AdmissionStatus admissionStatus) { this.admissionStatus = admissionStatus; }
    public Long getCurrentBedId() { return currentBedId; }
    public void setCurrentBedId(Long currentBedId) { this.currentBedId = currentBedId; }
    public String getCurrentBedNumber() { return currentBedNumber; }
    public void setCurrentBedNumber(String currentBedNumber) { this.currentBedNumber = currentBedNumber; }
    public Long getCurrentWardId() { return currentWardId; }
    public void setCurrentWardId(Long currentWardId) { this.currentWardId = currentWardId; }
    public String getCurrentWardName() { return currentWardName; }
    public void setCurrentWardName(String currentWardName) { this.currentWardName = currentWardName; }
    public Long getRecommendationId() { return recommendationId; }
    public void setRecommendationId(Long recommendationId) { this.recommendationId = recommendationId; }
    public Long getConsentId() { return consentId; }
    public void setConsentId(Long consentId) { this.consentId = consentId; }
    public Long getBedReservationId() { return bedReservationId; }
    public void setBedReservationId(Long bedReservationId) { this.bedReservationId = bedReservationId; }
    public Long getPatientTransferId() { return patientTransferId; }
    public void setPatientTransferId(Long patientTransferId) { this.patientTransferId = patientTransferId; }
    public String getOldBedStatus() { return oldBedStatus; }
    public void setOldBedStatus(String oldBedStatus) { this.oldBedStatus = oldBedStatus; }
    public String getNewBedStatus() { return newBedStatus; }
    public void setNewBedStatus(String newBedStatus) { this.newBedStatus = newBedStatus; }
    public String getSystemUpdateSummary() { return systemUpdateSummary; }
    public void setSystemUpdateSummary(String systemUpdateSummary) { this.systemUpdateSummary = systemUpdateSummary; }
    public Instant getTransferredAt() { return transferredAt; }
    public void setTransferredAt(Instant transferredAt) { this.transferredAt = transferredAt; }
}
