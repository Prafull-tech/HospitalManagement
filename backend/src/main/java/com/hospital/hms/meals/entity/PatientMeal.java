package com.hospital.hms.meals.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.dietary.entity.DietType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Patient meal record for IPD. Tracks meal delivery and serving status.
 */
@Entity
@Table(
    name = "patient_meals",
    indexes = {
        @Index(name = "idx_patient_meal_patient", columnList = "patient_id"),
        @Index(name = "idx_patient_meal_admission", columnList = "ipd_admission_id"),
        @Index(name = "idx_patient_meal_date", columnList = "meal_date"),
        @Index(name = "idx_patient_meal_status", columnList = "status")
    }
)
public class PatientMeal extends BaseIdEntity {

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "ipd_admission_id", nullable = false)
    private Long ipdAdmissionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType;

    @Enumerated(EnumType.STRING)
    @Column(name = "diet_type", nullable = false, length = 20)
    private DietType dietType;

    @Column(name = "delivered_by", length = 100)
    private String deliveredBy;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PatientMealStatus status = PatientMealStatus.PENDING;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    public PatientMeal() {
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
