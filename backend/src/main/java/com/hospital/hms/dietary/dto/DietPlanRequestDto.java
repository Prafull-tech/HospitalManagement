package com.hospital.hms.dietary.dto;

import com.hospital.hms.dietary.entity.DietType;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a diet plan (POST /api/dietary/plans).
 */
public class DietPlanRequestDto {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "IPD admission ID is required")
    private Long ipdAdmissionId;

    @NotNull(message = "Diet type is required")
    private DietType dietType;

    private String mealSchedule;

    private String createdByDoctor;

    private Boolean active = true;

    public DietPlanRequestDto() {
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }

    public DietType getDietType() {
        return dietType;
    }

    public void setDietType(DietType dietType) {
        this.dietType = dietType;
    }

    public String getMealSchedule() {
        return mealSchedule;
    }

    public void setMealSchedule(String mealSchedule) {
        this.mealSchedule = mealSchedule;
    }

    public String getCreatedByDoctor() {
        return createdByDoctor;
    }

    public void setCreatedByDoctor(String createdByDoctor) {
        this.createdByDoctor = createdByDoctor;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
