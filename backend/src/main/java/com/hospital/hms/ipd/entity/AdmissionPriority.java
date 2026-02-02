package com.hospital.hms.ipd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Admission Priority Master. Ensures critical patients get priority admission.
 * Lower priorityOrder = higher priority (1 = highest).
 * Priority codes (P1–P4) must be unique. DB-agnostic JPA.
 */
@Entity
@Table(
    name = "admission_priority",
    indexes = {
        @Index(name = "uk_admission_priority_code", columnList = "priority_code", unique = true),
        @Index(name = "idx_admission_priority_order", columnList = "priority_order"),
        @Index(name = "idx_admission_priority_active", columnList = "is_active")
    }
)
public class AdmissionPriority extends BaseIdEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority_code", nullable = false, unique = true, length = 10)
    private PriorityCode priorityCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private AdmissionCategory category;

    @Column(name = "description", length = 500)
    private String description;

    /** 1 = highest priority; lower value = higher priority. */
    @NotNull
    @Column(name = "priority_order", nullable = false)
    private Integer priorityOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    public AdmissionPriority() {
    }

    public PriorityCode getPriorityCode() {
        return priorityCode;
    }

    public void setPriorityCode(PriorityCode priorityCode) {
        this.priorityCode = priorityCode;
    }

    public AdmissionCategory getCategory() {
        return category;
    }

    public void setCategory(AdmissionCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPriorityOrder() {
        return priorityOrder;
    }

    public void setPriorityOrder(Integer priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
