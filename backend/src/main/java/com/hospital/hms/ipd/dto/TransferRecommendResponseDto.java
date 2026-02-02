package com.hospital.hms.ipd.dto;

import java.time.Instant;

/**
 * Response DTO for transfer recommend API.
 */
public class TransferRecommendResponseDto {

    private Long id;
    private Long ipdAdmissionId;
    private Long recommendedByDoctorId;
    private String fromWardType;
    private String toWardType;
    private Long indicationId;
    private String recommendationNotes;
    private Boolean emergencyFlag;
    private Instant recommendationTime;

    public TransferRecommendResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Instant getRecommendationTime() {
        return recommendationTime;
    }

    public void setRecommendationTime(Instant recommendationTime) {
        this.recommendationTime = recommendationTime;
    }
}
