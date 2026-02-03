package com.hospital.hms.ipd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.ipd.entity.PriorityCode;
import com.hospital.hms.reception.entity.Patient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * IPD admission. Links patient (Reception), doctor (Doctors). DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "ipd_admissions",
    indexes = {
        @Index(name = "idx_ipd_admission_number", columnList = "admission_number", unique = true),
        @Index(name = "idx_ipd_admission_status", columnList = "admission_status"),
        @Index(name = "idx_ipd_admission_patient", columnList = "patient_id"),
        @Index(name = "idx_ipd_admission_datetime", columnList = "admission_datetime"),
        @Index(name = "idx_ipd_admission_priority", columnList = "admission_priority")
    }
)
public class IPDAdmission extends BaseIdEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "admission_number", nullable = false, unique = true, length = 50)
    private String admissionNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_doctor_id", nullable = false)
    private Doctor primaryDoctor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "admission_type", nullable = false, length = 30)
    private AdmissionType admissionType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "admission_status", nullable = false, length = 30)
    private AdmissionStatus admissionStatus = AdmissionStatus.ADMITTED;

    @NotNull
    @Column(name = "admission_datetime", nullable = false)
    private LocalDateTime admissionDateTime;

    @Column(name = "discharge_datetime")
    private LocalDateTime dischargeDateTime;

    @Column(name = "opd_visit_id")
    private Long opdVisitId;

    @Size(max = 500)
    @Column(name = "remarks", length = 500)
    private String remarks;

    @Size(max = 1000)
    @Column(name = "diagnosis", length = 1000)
    private String diagnosis;

    @Column(name = "deposit_amount", precision = 15, scale = 2)
    private java.math.BigDecimal depositAmount;

    @Size(max = 255)
    @Column(name = "insurance_tpa", length = 255)
    private String insuranceTpa;

    @Size(max = 500)
    @Column(name = "admission_form_document_ref", length = 500)
    private String admissionFormDocumentRef;

    @Size(max = 500)
    @Column(name = "consent_form_document_ref", length = 500)
    private String consentFormDocumentRef;

    @Size(max = 500)
    @Column(name = "id_proof_document_ref", length = 500)
    private String idProofDocumentRef;

    @Size(max = 500)
    @Column(name = "discharge_remarks", length = 500)
    private String dischargeRemarks;

    /** Assigned admission priority (P1–P4). Calculated at admission or set by authority override. */
    @Column(name = "admission_priority", length = 10)
    @Enumerated(EnumType.STRING)
    private PriorityCode admissionPriority;

    /** Reason for priority assignment (e.g. "Evaluated: EMERGENCY → P1" or override reason). */
    @Size(max = 500)
    @Column(name = "priority_assignment_reason", length = 500)
    private String priorityAssignmentReason;

    /** True if priority was manually overridden by authorised user. */
    @Column(name = "priority_overridden", nullable = false)
    private Boolean priorityOverridden = false;

    /** Username/identifier of authority who overrode priority. Null if not overridden. */
    @Column(name = "priority_override_by", length = 255)
    private String priorityOverrideBy;

    @Column(name = "priority_override_at")
    private Instant priorityOverrideAt;

    /** When nursing staff performed shift-to-ward (mandatory for shift workflow). */
    @Column(name = "shifted_to_ward_at")
    private Instant shiftedToWardAt;

    @Size(max = 255)
    @Column(name = "shifted_to_ward_by", length = 255)
    private String shiftedToWardBy;

    public IPDAdmission() {
    }

    public String getAdmissionNumber() {
        return admissionNumber;
    }

    public void setAdmissionNumber(String admissionNumber) {
        this.admissionNumber = admissionNumber;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getPrimaryDoctor() {
        return primaryDoctor;
    }

    public void setPrimaryDoctor(Doctor primaryDoctor) {
        this.primaryDoctor = primaryDoctor;
    }

    public AdmissionType getAdmissionType() {
        return admissionType;
    }

    public void setAdmissionType(AdmissionType admissionType) {
        this.admissionType = admissionType;
    }

    public AdmissionStatus getAdmissionStatus() {
        return admissionStatus;
    }

    public void setAdmissionStatus(AdmissionStatus admissionStatus) {
        this.admissionStatus = admissionStatus;
    }

    public LocalDateTime getAdmissionDateTime() {
        return admissionDateTime;
    }

    public void setAdmissionDateTime(LocalDateTime admissionDateTime) {
        this.admissionDateTime = admissionDateTime;
    }

    public LocalDateTime getDischargeDateTime() {
        return dischargeDateTime;
    }

    public void setDischargeDateTime(LocalDateTime dischargeDateTime) {
        this.dischargeDateTime = dischargeDateTime;
    }

    public Long getOpdVisitId() {
        return opdVisitId;
    }

    public void setOpdVisitId(Long opdVisitId) {
        this.opdVisitId = opdVisitId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public java.math.BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(java.math.BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public String getInsuranceTpa() {
        return insuranceTpa;
    }

    public void setInsuranceTpa(String insuranceTpa) {
        this.insuranceTpa = insuranceTpa;
    }

    public String getAdmissionFormDocumentRef() {
        return admissionFormDocumentRef;
    }

    public void setAdmissionFormDocumentRef(String admissionFormDocumentRef) {
        this.admissionFormDocumentRef = admissionFormDocumentRef;
    }

    public String getConsentFormDocumentRef() {
        return consentFormDocumentRef;
    }

    public void setConsentFormDocumentRef(String consentFormDocumentRef) {
        this.consentFormDocumentRef = consentFormDocumentRef;
    }

    public String getIdProofDocumentRef() {
        return idProofDocumentRef;
    }

    public void setIdProofDocumentRef(String idProofDocumentRef) {
        this.idProofDocumentRef = idProofDocumentRef;
    }

    public String getDischargeRemarks() {
        return dischargeRemarks;
    }

    public void setDischargeRemarks(String dischargeRemarks) {
        this.dischargeRemarks = dischargeRemarks;
    }

    public PriorityCode getAdmissionPriority() {
        return admissionPriority;
    }

    public void setAdmissionPriority(PriorityCode admissionPriority) {
        this.admissionPriority = admissionPriority;
    }

    public String getPriorityAssignmentReason() {
        return priorityAssignmentReason;
    }

    public void setPriorityAssignmentReason(String priorityAssignmentReason) {
        this.priorityAssignmentReason = priorityAssignmentReason;
    }

    public Boolean getPriorityOverridden() {
        return priorityOverridden;
    }

    public void setPriorityOverridden(Boolean priorityOverridden) {
        this.priorityOverridden = priorityOverridden;
    }

    public String getPriorityOverrideBy() {
        return priorityOverrideBy;
    }

    public void setPriorityOverrideBy(String priorityOverrideBy) {
        this.priorityOverrideBy = priorityOverrideBy;
    }

    public Instant getPriorityOverrideAt() {
        return priorityOverrideAt;
    }

    public void setPriorityOverrideAt(Instant priorityOverrideAt) {
        this.priorityOverrideAt = priorityOverrideAt;
    }

    public Instant getShiftedToWardAt() {
        return shiftedToWardAt;
    }

    public void setShiftedToWardAt(Instant shiftedToWardAt) {
        this.shiftedToWardAt = shiftedToWardAt;
    }

    public String getShiftedToWardBy() {
        return shiftedToWardBy;
    }

    public void setShiftedToWardBy(String shiftedToWardBy) {
        this.shiftedToWardBy = shiftedToWardBy;
    }
}
