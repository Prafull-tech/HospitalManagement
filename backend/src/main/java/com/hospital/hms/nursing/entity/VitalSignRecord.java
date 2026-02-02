package com.hospital.hms.nursing.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.ipd.entity.IPDAdmission;
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
 * Vital signs record for an IPD admission. DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "vital_sign_records",
    indexes = {
        @Index(name = "idx_vital_ipd_admission", columnList = "ipd_admission_id"),
        @Index(name = "idx_vital_recorded_at", columnList = "recorded_at"),
        @Index(name = "idx_vital_nurse", columnList = "recorded_by_id")
    }
)
public class VitalSignRecord extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipd_admission_id", nullable = false)
    private IPDAdmission ipdAdmission;

    @NotNull
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "blood_pressure_systolic")
    private Integer bloodPressureSystolic;

    @Column(name = "blood_pressure_diastolic")
    private Integer bloodPressureDiastolic;

    @Column(name = "pulse")
    private Integer pulse;

    @Column(name = "temperature")
    private java.math.BigDecimal temperature;

    @Column(name = "spo2")
    private Integer spo2;

    @Column(name = "respiration")
    private Integer respiration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id")
    private NursingStaff recordedBy;

    @Size(max = 500)
    @Column(name = "remarks", length = 500)
    private String remarks;

    public VitalSignRecord() {
    }

    public IPDAdmission getIpdAdmission() {
        return ipdAdmission;
    }

    public void setIpdAdmission(IPDAdmission ipdAdmission) {
        this.ipdAdmission = ipdAdmission;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public Integer getBloodPressureSystolic() {
        return bloodPressureSystolic;
    }

    public void setBloodPressureSystolic(Integer bloodPressureSystolic) {
        this.bloodPressureSystolic = bloodPressureSystolic;
    }

    public Integer getBloodPressureDiastolic() {
        return bloodPressureDiastolic;
    }

    public void setBloodPressureDiastolic(Integer bloodPressureDiastolic) {
        this.bloodPressureDiastolic = bloodPressureDiastolic;
    }

    public Integer getPulse() {
        return pulse;
    }

    public void setPulse(Integer pulse) {
        this.pulse = pulse;
    }

    public java.math.BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(java.math.BigDecimal temperature) {
        this.temperature = temperature;
    }

    public Integer getSpo2() {
        return spo2;
    }

    public void setSpo2(Integer spo2) {
        this.spo2 = spo2;
    }

    public Integer getRespiration() {
        return respiration;
    }

    public void setRespiration(Integer respiration) {
        this.respiration = respiration;
    }

    public NursingStaff getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(NursingStaff recordedBy) {
        this.recordedBy = recordedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
