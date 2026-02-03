package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.ConsentMode;
import com.hospital.hms.ipd.entity.EquipmentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Single request for POST /api/ipd/transfers: full workflow in one call.
 * Steps: Doctor recommendation → Family consent → Bed selection → Patient shift.
 * All validations (consent, bed available) apply; emergency flag can bypass consent when enforced by service.
 */
public class IPDTransferFullRequestDto {

    // ——— Step 1: Doctor recommendation ———
    @NotNull(message = "IPD admission ID is required")
    private Long ipdAdmissionId;

    @NotNull(message = "Recommending doctor ID is required")
    private Long recommendedByDoctorId;

    @NotNull(message = "From ward type is required (e.g. GENERAL, ICU)")
    @Size(max = 30)
    private String fromWardType;

    @NotNull(message = "To ward type is required (e.g. ICU, PRIVATE)")
    @Size(max = 30)
    private String toWardType;

    @Size(max = 1000)
    private String recommendationNotes;

    private Boolean emergencyFlag;

    // ——— Step 2: Family consent ———
    @NotNull(message = "Consent given is required")
    private Boolean consentGiven;

    @Size(max = 255)
    private String consentByName;

    @Size(max = 100)
    private String relationToPatient;

    @NotNull(message = "Consent mode is required")
    private ConsentMode consentMode;

    // ——— Step 3: Bed selection ———
    @NotNull(message = "New bed ID is required")
    private Long newBedId;

    // ——— Step 4: Patient shift (optional execution details) ———
    private Long nurseId;
    private Long attendantId;
    private EquipmentType equipmentUsed;

    @Size(max = 20)
    private String transferStatus;

    @Size(max = 500)
    private String remarks;

    public IPDTransferFullRequestDto() {
    }

    public Long getIpdAdmissionId() { return ipdAdmissionId; }
    public void setIpdAdmissionId(Long ipdAdmissionId) { this.ipdAdmissionId = ipdAdmissionId; }
    public Long getRecommendedByDoctorId() { return recommendedByDoctorId; }
    public void setRecommendedByDoctorId(Long recommendedByDoctorId) { this.recommendedByDoctorId = recommendedByDoctorId; }
    public String getFromWardType() { return fromWardType; }
    public void setFromWardType(String fromWardType) { this.fromWardType = fromWardType; }
    public String getToWardType() { return toWardType; }
    public void setToWardType(String toWardType) { this.toWardType = toWardType; }
    public String getRecommendationNotes() { return recommendationNotes; }
    public void setRecommendationNotes(String recommendationNotes) { this.recommendationNotes = recommendationNotes; }
    public Boolean getEmergencyFlag() { return emergencyFlag; }
    public void setEmergencyFlag(Boolean emergencyFlag) { this.emergencyFlag = emergencyFlag; }
    public Boolean getConsentGiven() { return consentGiven; }
    public void setConsentGiven(Boolean consentGiven) { this.consentGiven = consentGiven; }
    public String getConsentByName() { return consentByName; }
    public void setConsentByName(String consentByName) { this.consentByName = consentByName; }
    public String getRelationToPatient() { return relationToPatient; }
    public void setRelationToPatient(String relationToPatient) { this.relationToPatient = relationToPatient; }
    public ConsentMode getConsentMode() { return consentMode; }
    public void setConsentMode(ConsentMode consentMode) { this.consentMode = consentMode; }
    public Long getNewBedId() { return newBedId; }
    public void setNewBedId(Long newBedId) { this.newBedId = newBedId; }
    public Long getNurseId() { return nurseId; }
    public void setNurseId(Long nurseId) { this.nurseId = nurseId; }
    public Long getAttendantId() { return attendantId; }
    public void setAttendantId(Long attendantId) { this.attendantId = attendantId; }
    public EquipmentType getEquipmentUsed() { return equipmentUsed; }
    public void setEquipmentUsed(EquipmentType equipmentUsed) { this.equipmentUsed = equipmentUsed; }
    public String getTransferStatus() { return transferStatus; }
    public void setTransferStatus(String transferStatus) { this.transferStatus = transferStatus; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
