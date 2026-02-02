package com.hospital.hms.ipd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Special Admission Consideration configuration. Configurable priority boost for special categories.
 * Boost improves effective priority in queue; application logic must never let boost override P1 emergency.
 * One record per considerationType. DB-agnostic JPA.
 */
@Entity
@Table(
    name = "special_admission_consideration",
    indexes = {
        @Index(name = "uk_special_consideration_type", columnList = "consideration_type", unique = true),
        @Index(name = "idx_special_consideration_active", columnList = "is_active")
    }
)
public class SpecialAdmissionConsideration extends BaseIdEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "consideration_type", nullable = false, unique = true, length = 50)
    private SpecialConsiderationType considerationType;

    /** Configurable boost (e.g. points or order offset). Applied in logic; must not override P1 emergency. */
    @NotNull
    @Min(0)
    @Column(name = "priority_boost", nullable = false)
    private Integer priorityBoost = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    public SpecialAdmissionConsideration() {
    }

    public SpecialConsiderationType getConsiderationType() {
        return considerationType;
    }

    public void setConsiderationType(SpecialConsiderationType considerationType) {
        this.considerationType = considerationType;
    }

    public Integer getPriorityBoost() {
        return priorityBoost;
    }

    public void setPriorityBoost(Integer priorityBoost) {
        this.priorityBoost = priorityBoost;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
