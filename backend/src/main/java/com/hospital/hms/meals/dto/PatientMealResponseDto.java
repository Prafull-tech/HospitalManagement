package com.hospital.hms.meals.dto;

import com.hospital.hms.dietary.entity.DietType;
import com.hospital.hms.meals.entity.MealType;
import com.hospital.hms.meals.entity.PatientMealStatus;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for patient meal (GET /api/meals/today).
 */
public class PatientMealResponseDto {

    private Long id;
    private Long patientId;
    private Long ipdAdmissionId;
    private MealType mealType;
    private DietType dietType;
    private String deliveredBy;
    private Instant deliveredAt;
    private PatientMealStatus status;
    private LocalDate mealDate;

    public PatientMealResponseDto() {
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

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public DietType getDietType() {
        return dietType;
    }

    public void setDietType(DietType dietType) {
        this.dietType = dietType;
    }

    public String getDeliveredBy() {
        return deliveredBy;
    }

    public void setDeliveredBy(String deliveredBy) {
        this.deliveredBy = deliveredBy;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public PatientMealStatus getStatus() {
        return status;
    }

    public void setStatus(PatientMealStatus status) {
        this.status = status;
    }

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
    }
}
