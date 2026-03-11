package com.hospital.hms.ipd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * IPD Patient Discharge record with departmental clearance tracking.
 * NABH / medico-legal compliant. All clearances must be true before final discharge.
 */
@Entity
@Table(
    name = "patient_discharges",
    indexes = {
        @Index(name = "idx_discharge_ipd", columnList = "ipd_admission_id", unique = true),
        @Index(name = "idx_discharge_patient", columnList = "patient_id"),
        @Index(name = "idx_discharge_uhid", columnList = "uhid")
    }
)
public class PatientDischarge extends BaseIdEntity {

    @NotNull
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @NotNull
    @Size(max = 50)
    @Column(name = "uhid", nullable = false, length = 50)
    private String uhid;

    @NotNull
    @Column(name = "ipd_admission_id", nullable = false, unique = true)
    private Long ipdAdmissionId;

    @Column(name = "bed_id")
    private Long bedId;

    @Size(max = 30)
    @Column(name = "ward_type", length = 30)
    private String wardType;

    @NotNull
    @Column(name = "admitted_date", nullable = false)
    private LocalDateTime admittedDate;

    @Column(name = "discharge_date")
    private LocalDateTime dischargeDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "discharge_type", nullable = false, length = 20)
    private DischargeType dischargeType = DischargeType.NORMAL;

    @Column(name = "doctor_clearance", nullable = false)
    private Boolean doctorClearance = false;

    @Column(name = "nursing_clearance", nullable = false)
    private Boolean nursingClearance = false;

    @Column(name = "pharmacy_clearance", nullable = false)
    private Boolean pharmacyClearance = false;

    @Column(name = "lab_clearance", nullable = false)
    private Boolean labClearance = false;

    @Column(name = "billing_clearance", nullable = false)
    private Boolean billingClearance = false;

    @Column(name = "insurance_clearance", nullable = false)
    private Boolean insuranceClearance = false;

    @Column(name = "housekeeping_clearance", nullable = false)
    private Boolean housekeepingClearance = false;

    @Column(name = "linen_clearance", nullable = false)
    private Boolean linenClearance = false;

    @Column(name = "dietary_clearance", nullable = false)
    private Boolean dietaryClearance = false;

    @Size(max = 255)
    @Column(name = "doctor_cleared_by", length = 255)
    private String doctorClearedBy;

    @Column(name = "doctor_cleared_at")
    private Instant doctorClearedAt;

    @Size(max = 255)
    @Column(name = "nursing_cleared_by", length = 255)
    private String nursingClearedBy;

    @Column(name = "nursing_cleared_at")
    private Instant nursingClearedAt;

    @Size(max = 255)
    @Column(name = "pharmacy_cleared_by", length = 255)
    private String pharmacyClearedBy;

    @Column(name = "pharmacy_cleared_at")
    private Instant pharmacyClearedAt;

    @Size(max = 255)
    @Column(name = "lab_cleared_by", length = 255)
    private String labClearedBy;

    @Column(name = "lab_cleared_at")
    private Instant labClearedAt;

    @Size(max = 255)
    @Column(name = "billing_cleared_by", length = 255)
    private String billingClearedBy;

    @Column(name = "billing_cleared_at")
    private Instant billingClearedAt;

    @Size(max = 255)
    @Column(name = "discharged_by", length = 255)
    private String dischargedBy;

    @Column(name = "discharged_at")
    private Instant dischargedAt;

    @Size(max = 100)
    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Size(max = 2000)
    @Column(name = "diagnosis_summary", length = 2000)
    private String diagnosisSummary;

    @Size(max = 2000)
    @Column(name = "treatment_summary", length = 2000)
    private String treatmentSummary;

    @Size(max = 1000)
    @Column(name = "procedures", length = 1000)
    private String procedures;

    @Size(max = 1000)
    @Column(name = "advice", length = 1000)
    private String advice;

    @Size(max = 1000)
    @Column(name = "follow_up", length = 1000)
    private String followUp;

    @Size(max = 1000)
    @Column(name = "medicines_on_discharge", length = 1000)
    private String medicinesOnDischarge;

    public PatientDischarge() {
    }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getUhid() { return uhid; }
    public void setUhid(String uhid) { this.uhid = uhid; }
    public Long getIpdAdmissionId() { return ipdAdmissionId; }
    public void setIpdAdmissionId(Long ipdAdmissionId) { this.ipdAdmissionId = ipdAdmissionId; }
    public Long getBedId() { return bedId; }
    public void setBedId(Long bedId) { this.bedId = bedId; }
    public String getWardType() { return wardType; }
    public void setWardType(String wardType) { this.wardType = wardType; }
    public LocalDateTime getAdmittedDate() { return admittedDate; }
    public void setAdmittedDate(LocalDateTime admittedDate) { this.admittedDate = admittedDate; }
    public LocalDateTime getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(LocalDateTime dischargeDate) { this.dischargeDate = dischargeDate; }
    public DischargeType getDischargeType() { return dischargeType; }
    public void setDischargeType(DischargeType dischargeType) { this.dischargeType = dischargeType; }
    public Boolean getDoctorClearance() { return doctorClearance; }
    public void setDoctorClearance(Boolean doctorClearance) { this.doctorClearance = doctorClearance; }
    public Boolean getNursingClearance() { return nursingClearance; }
    public void setNursingClearance(Boolean nursingClearance) { this.nursingClearance = nursingClearance; }
    public Boolean getPharmacyClearance() { return pharmacyClearance; }
    public void setPharmacyClearance(Boolean pharmacyClearance) { this.pharmacyClearance = pharmacyClearance; }
    public Boolean getLabClearance() { return labClearance; }
    public void setLabClearance(Boolean labClearance) { this.labClearance = labClearance; }
    public Boolean getBillingClearance() { return billingClearance; }
    public void setBillingClearance(Boolean billingClearance) { this.billingClearance = billingClearance; }
    public Boolean getInsuranceClearance() { return insuranceClearance; }
    public void setInsuranceClearance(Boolean insuranceClearance) { this.insuranceClearance = insuranceClearance; }
    public Boolean getHousekeepingClearance() { return housekeepingClearance; }
    public void setHousekeepingClearance(Boolean housekeepingClearance) { this.housekeepingClearance = housekeepingClearance; }
    public Boolean getLinenClearance() { return linenClearance; }
    public void setLinenClearance(Boolean linenClearance) { this.linenClearance = linenClearance; }
    public Boolean getDietaryClearance() { return dietaryClearance; }
    public void setDietaryClearance(Boolean dietaryClearance) { this.dietaryClearance = dietaryClearance; }
    public String getDoctorClearedBy() { return doctorClearedBy; }
    public void setDoctorClearedBy(String doctorClearedBy) { this.doctorClearedBy = doctorClearedBy; }
    public Instant getDoctorClearedAt() { return doctorClearedAt; }
    public void setDoctorClearedAt(Instant doctorClearedAt) { this.doctorClearedAt = doctorClearedAt; }
    public String getNursingClearedBy() { return nursingClearedBy; }
    public void setNursingClearedBy(String nursingClearedBy) { this.nursingClearedBy = nursingClearedBy; }
    public Instant getNursingClearedAt() { return nursingClearedAt; }
    public void setNursingClearedAt(Instant nursingClearedAt) { this.nursingClearedAt = nursingClearedAt; }
    public String getPharmacyClearedBy() { return pharmacyClearedBy; }
    public void setPharmacyClearedBy(String pharmacyClearedBy) { this.pharmacyClearedBy = pharmacyClearedBy; }
    public Instant getPharmacyClearedAt() { return pharmacyClearedAt; }
    public void setPharmacyClearedAt(Instant pharmacyClearedAt) { this.pharmacyClearedAt = pharmacyClearedAt; }
    public String getLabClearedBy() { return labClearedBy; }
    public void setLabClearedBy(String labClearedBy) { this.labClearedBy = labClearedBy; }
    public Instant getLabClearedAt() { return labClearedAt; }
    public void setLabClearedAt(Instant labClearedAt) { this.labClearedAt = labClearedAt; }
    public String getBillingClearedBy() { return billingClearedBy; }
    public void setBillingClearedBy(String billingClearedBy) { this.billingClearedBy = billingClearedBy; }
    public Instant getBillingClearedAt() { return billingClearedAt; }
    public void setBillingClearedAt(Instant billingClearedAt) { this.billingClearedAt = billingClearedAt; }
    public String getDischargedBy() { return dischargedBy; }
    public void setDischargedBy(String dischargedBy) { this.dischargedBy = dischargedBy; }
    public Instant getDischargedAt() { return dischargedAt; }
    public void setDischargedAt(Instant dischargedAt) { this.dischargedAt = dischargedAt; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getDiagnosisSummary() { return diagnosisSummary; }
    public void setDiagnosisSummary(String diagnosisSummary) { this.diagnosisSummary = diagnosisSummary; }
    public String getTreatmentSummary() { return treatmentSummary; }
    public void setTreatmentSummary(String treatmentSummary) { this.treatmentSummary = treatmentSummary; }
    public String getProcedures() { return procedures; }
    public void setProcedures(String procedures) { this.procedures = procedures; }
    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }
    public String getFollowUp() { return followUp; }
    public void setFollowUp(String followUp) { this.followUp = followUp; }
    public String getMedicinesOnDischarge() { return medicinesOnDischarge; }
    public void setMedicinesOnDischarge(String medicinesOnDischarge) { this.medicinesOnDischarge = medicinesOnDischarge; }
}
