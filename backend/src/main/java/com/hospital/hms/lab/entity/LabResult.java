package com.hospital.hms.lab.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Laboratory test result values entered by lab technician.
 * Linked to TestOrder. Multiple results possible for panels or multi-parameter tests.
 */
@Entity
@Table(
    name = "lab_results",
    indexes = {
        @Index(name = "idx_lab_result_order", columnList = "test_order_id"),
        @Index(name = "idx_lab_result_entered_at", columnList = "entered_at")
    }
)
public class LabResult extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_order_id", nullable = false)
    private TestOrder testOrder;

    @Size(max = 255)
    @Column(name = "parameter_name", length = 255)
    private String parameterName; // e.g., "Hemoglobin", "WBC Count"

    @Size(max = 1000)
    @Column(name = "result_value", length = 1000)
    private String resultValue; // Actual result value

    @Size(max = 100)
    @Column(name = "unit", length = 100)
    private String unit; // e.g., "g/dL", "cells/μL"

    @Size(max = 1000)
    @Column(name = "normal_range", length = 1000)
    private String normalRange; // Normal range for this parameter

    @Size(max = 50)
    @Column(name = "flag", length = 50)
    private String flag; // H (High), L (Low), N (Normal), CRITICAL

    @NotNull
    @Column(name = "entered_at", nullable = false)
    private LocalDateTime enteredAt;

    @Size(max = 255)
    @Column(name = "entered_by", length = 255)
    private String enteredBy; // Lab technician username

    @Size(max = 1000)
    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "is_critical", nullable = false)
    private Boolean isCritical = false; // Critical value flag

    public LabResult() {
    }

    public TestOrder getTestOrder() {
        return testOrder;
    }

    public void setTestOrder(TestOrder testOrder) {
        this.testOrder = testOrder;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getNormalRange() {
        return normalRange;
    }

    public void setNormalRange(String normalRange) {
        this.normalRange = normalRange;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public LocalDateTime getEnteredAt() {
        return enteredAt;
    }

    public void setEnteredAt(LocalDateTime enteredAt) {
        this.enteredAt = enteredAt;
    }

    public String getEnteredBy() {
        return enteredBy;
    }

    public void setEnteredBy(String enteredBy) {
        this.enteredBy = enteredBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Boolean getIsCritical() {
        return isCritical;
    }

    public void setIsCritical(Boolean isCritical) {
        this.isCritical = isCritical;
    }
}
