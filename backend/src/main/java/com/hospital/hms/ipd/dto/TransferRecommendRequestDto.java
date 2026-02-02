package com.hospital.hms.ipd.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for POST /api/ipd/transfers/recommend.
 */
public class TransferRecommendRequestDto {

    @NotNull(message = "IPD admission ID is required")
    private Long ipdAdmissionId;

    @NotNull(message = "Recommending doctor ID is required")
    private Long recommendedByDoctorId;

    @NotNull(message = "From ward type is required")
    @Size(max = 30)
    private String fromWardType;

    @NotNull(message = "To ward type is required")
    @Size(max = 30)
    private String toWardType;

    private Long indicationId;

    @Size(max = 1000)
    private String recommendationNotes;

    private Boolean emergencyFlag;

    public TransferRecommendRequestDto() {
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }

    public Long getRecommendedByDoctorId() {
        return recommendedByDoctorId;
    }

    public void setRecommendedByDoctorId(Long recommendedByDoctorId) {
        this.recommendedByDoctorId = recommendedByDoctorId;
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

    public Long getIndicationId() {
        return indicationId;
    }

    public void setIndicationId(Long indicationId) {
        this.indicationId = indicationId;
    }

    public String getRecommendationNotes() {
        return recommendationNotes;
    }

    public void setRecommendationNotes(String recommendationNotes) {
        this.recommendationNotes = recommendationNotes;
    }

    public Boolean getEmergencyFlag() {
        return emergencyFlag;
    }

    public void setEmergencyFlag(Boolean emergencyFlag) {
        this.emergencyFlag = emergencyFlag;
    }
}
