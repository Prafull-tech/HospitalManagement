package com.hospital.hms.dietary.dto;

import com.hospital.hms.dietary.entity.DietType;

import java.time.Instant;

/**
 * Response DTO for diet plan (GET /api/dietary/plans).
 */
public class DietPlanResponseDto {

    private Long id;
    private Long patientId;
    private Long ipdAdmissionId;
    private DietType dietType;
    private String mealSchedule;
    private String createdByDoctor;
    private boolean active;
    private Instant createdAt;

    public DietPlanResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
