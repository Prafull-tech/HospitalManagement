package com.hospital.hms.dietary.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Diet plan prescribed for an IPD patient.
 * Links to patient and admission; created by doctor.
 */
@Entity
@Table(
    name = "diet_plans",
    indexes = {
        @Index(name = "idx_diet_plan_patient", columnList = "patient_id"),
        @Index(name = "idx_diet_plan_admission", columnList = "ipd_admission_id"),
        @Index(name = "idx_diet_plan_active", columnList = "active"),
        @Index(name = "idx_diet_plan_created", columnList = "created_at")
    }
)
public class DietPlan extends BaseIdEntity {

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "ipd_admission_id", nullable = false)
    private Long ipdAdmissionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "diet_type", nullable = false, length = 20)
    private DietType dietType;

    @Column(name = "meal_schedule", columnDefinition = "TEXT")
    private String mealSchedule;

    @Column(name = "created_by_doctor", length = 100)
    private String createdByDoctor;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public DietPlan() {
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
}
