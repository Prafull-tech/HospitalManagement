package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.DischargeType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Real-time discharge clearance status for IPD admission.
 */
public class DischargeStatusDto {

    private Long ipdAdmissionId;
    private String admissionNumber;
    private Long patientId;
    private String uhid;
    private String patientName;
    private Long bedId;
    private String bedNumber;
    private String wardName;
    private LocalDateTime admittedDate;
    private LocalDateTime dischargeDate;
    private DischargeType dischargeType;
    private String admissionStatus;

    private boolean doctorClearance;
    private boolean nursingClearance;
    private boolean pharmacyClearance;
    private boolean labClearance;
    private boolean billingClearance;
    private boolean insuranceClearance;
    private boolean housekeepingClearance;
    private boolean linenClearance;
    private boolean dietaryClearance;

    private int pendingPharmacyCount;
    private int pendingLabCount;
    private java.math.BigDecimal billingTotal;
    private java.math.BigDecimal billingPendingAmount;
    private boolean billingPaid;

    private List<DischargePendingItemDto> pendingPharmacy;
    private List<DischargePendingItemDto> pendingLab;

    private boolean allClearancesComplete;
    private boolean canFinalizeDischarge;

    // Discharge summary (doctor fills)
    private String diagnosisSummary;
    private String treatmentSummary;
    private String procedures;
    private String advice;
    private String followUp;
    private String medicinesOnDischarge;

    public Long getIpdAdmissionId() { return ipdAdmissionId; }
    public void setIpdAdmissionId(Long ipdAdmissionId) { this.ipdAdmissionId = ipdAdmissionId; }
    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getUhid() { return uhid; }
    public void setUhid(String uhid) { this.uhid = uhid; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public Long getBedId() { return bedId; }
    public void setBedId(Long bedId) { this.bedId = bedId; }
    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }
    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }
    public LocalDateTime getAdmittedDate() { return admittedDate; }
    public void setAdmittedDate(LocalDateTime admittedDate) { this.admittedDate = admittedDate; }
    public LocalDateTime getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(LocalDateTime dischargeDate) { this.dischargeDate = dischargeDate; }
    public DischargeType getDischargeType() { return dischargeType; }
    public void setDischargeType(DischargeType dischargeType) { this.dischargeType = dischargeType; }
    public String getAdmissionStatus() { return admissionStatus; }
    public void setAdmissionStatus(String admissionStatus) { this.admissionStatus = admissionStatus; }
    public boolean isDoctorClearance() { return doctorClearance; }
    public void setDoctorClearance(boolean doctorClearance) { this.doctorClearance = doctorClearance; }
    public boolean isNursingClearance() { return nursingClearance; }
    public void setNursingClearance(boolean nursingClearance) { this.nursingClearance = nursingClearance; }
    public boolean isPharmacyClearance() { return pharmacyClearance; }
    public void setPharmacyClearance(boolean pharmacyClearance) { this.pharmacyClearance = pharmacyClearance; }
    public boolean isLabClearance() { return labClearance; }
    public void setLabClearance(boolean labClearance) { this.labClearance = labClearance; }
    public boolean isBillingClearance() { return billingClearance; }
    public void setBillingClearance(boolean billingClearance) { this.billingClearance = billingClearance; }
    public boolean isInsuranceClearance() { return insuranceClearance; }
    public void setInsuranceClearance(boolean insuranceClearance) { this.insuranceClearance = insuranceClearance; }
    public boolean isHousekeepingClearance() { return housekeepingClearance; }
    public void setHousekeepingClearance(boolean housekeepingClearance) { this.housekeepingClearance = housekeepingClearance; }
    public boolean isLinenClearance() { return linenClearance; }
    public void setLinenClearance(boolean linenClearance) { this.linenClearance = linenClearance; }
    public boolean isDietaryClearance() { return dietaryClearance; }
    public void setDietaryClearance(boolean dietaryClearance) { this.dietaryClearance = dietaryClearance; }
    public int getPendingPharmacyCount() { return pendingPharmacyCount; }
    public void setPendingPharmacyCount(int pendingPharmacyCount) { this.pendingPharmacyCount = pendingPharmacyCount; }
    public int getPendingLabCount() { return pendingLabCount; }
    public void setPendingLabCount(int pendingLabCount) { this.pendingLabCount = pendingLabCount; }
    public java.math.BigDecimal getBillingTotal() { return billingTotal; }
    public void setBillingTotal(java.math.BigDecimal billingTotal) { this.billingTotal = billingTotal; }
    public java.math.BigDecimal getBillingPendingAmount() { return billingPendingAmount; }
    public void setBillingPendingAmount(java.math.BigDecimal billingPendingAmount) { this.billingPendingAmount = billingPendingAmount; }
    public boolean isBillingPaid() { return billingPaid; }
    public void setBillingPaid(boolean billingPaid) { this.billingPaid = billingPaid; }
    public List<DischargePendingItemDto> getPendingPharmacy() { return pendingPharmacy; }
    public void setPendingPharmacy(List<DischargePendingItemDto> pendingPharmacy) { this.pendingPharmacy = pendingPharmacy; }
    public List<DischargePendingItemDto> getPendingLab() { return pendingLab; }
    public void setPendingLab(List<DischargePendingItemDto> pendingLab) { this.pendingLab = pendingLab; }
    public boolean isAllClearancesComplete() { return allClearancesComplete; }
    public void setAllClearancesComplete(boolean allClearancesComplete) { this.allClearancesComplete = allClearancesComplete; }
    public boolean isCanFinalizeDischarge() { return canFinalizeDischarge; }
    public void setCanFinalizeDischarge(boolean canFinalizeDischarge) { this.canFinalizeDischarge = canFinalizeDischarge; }
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
