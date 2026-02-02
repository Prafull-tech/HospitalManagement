package com.hospital.hms.ipd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Admission Priority Rules configuration. Maps condition types to priority (P1–P4).
 * One rule per conditionType. DB-agnostic JPA.
 * <ul>
 *   <li>Emergency patients → P1</li>
 *   <li>ICU patients → P2</li>
 *   <li>Referred cases → P3</li>
 *   <li>Elective admissions → P4</li>
 * </ul>
 */
@Entity
@Table(
    name = "admission_priority_rule",
    indexes = {
        @Index(name = "uk_admission_priority_rule_condition", columnList = "condition_type", unique = true),
        @Index(name = "idx_admission_priority_rule_active", columnList = "is_active")
    }
)
public class AdmissionPriorityRule extends BaseIdEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, unique = true, length = 50)
    private AdmissionConditionType conditionType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "mapped_priority", nullable = false, length = 10)
    private PriorityCode mappedPriority;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    public AdmissionPriorityRule() {
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public AdmissionConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(AdmissionConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public PriorityCode getMappedPriority() {
        return mappedPriority;
    }

    public void setMappedPriority(PriorityCode mappedPriority) {
        this.mappedPriority = mappedPriority;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
