package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.entity.AdmissionType;
import com.hospital.hms.ipd.entity.PriorityCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Response DTO for IPD admission (view/list).
 */
public class IPDAdmissionResponseDto {

    private Long id;
    private String admissionNumber;
    private String patientUhid;
    private Long patientId;
    private String patientName;
    private Long primaryDoctorId;
    private String primaryDoctorName;
    private String primaryDoctorCode;
    private AdmissionType admissionType;
    private AdmissionStatus admissionStatus;
    private LocalDateTime admissionDateTime;
    private LocalDateTime dischargeDateTime;
    private Long opdVisitId;
    private String remarks;
    private String diagnosis;
    private BigDecimal depositAmount;
    private String insuranceTpa;
    private String admissionFormDocumentRef;
    private String consentFormDocumentRef;
    private String idProofDocumentRef;
    private String dischargeRemarks;
    private Long currentWardId;
    private String currentWardName;
    private String currentRoomNumber;
    private Long currentBedId;
    private String currentBedNumber;
    private Instant createdAt;
    private Instant updatedAt;

    /** Assigned admission priority (P1–P4). */
    private PriorityCode admissionPriority;
    /** Reason for priority assignment (evaluated or override). */
    private String priorityAssignmentReason;
    /** True if priority was overridden by authority. */
    private Boolean priorityOverridden;
    /** Username of authority who overrode. */
    private String priorityOverrideBy;
    /** When priority was overridden. */
    private Instant priorityOverrideAt;
    /** When nursing staff performed shift-to-ward. */
    private Instant shiftedToWardAt;
    /** Username of nursing staff who performed shift. */
    private String shiftedToWardBy;

    public IPDAdmissionResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdmissionNumber() {
        return admissionNumber;
    }

    public void setAdmissionNumber(String admissionNumber) {
        this.admissionNumber = admissionNumber;
    }

    public String getPatientUhid() {
        return patientUhid;
    }

    public void setPatientUhid(String patientUhid) {
        this.patientUhid = patientUhid;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Long getPrimaryDoctorId() {
        return primaryDoctorId;
    }

    public void setPrimaryDoctorId(Long primaryDoctorId) {
        this.primaryDoctorId = primaryDoctorId;
    }

    public String getPrimaryDoctorName() {
        return primaryDoctorName;
    }

    public void setPrimaryDoctorName(String primaryDoctorName) {
        this.primaryDoctorName = primaryDoctorName;
    }

    public String getPrimaryDoctorCode() {
        return primaryDoctorCode;
    }

    public void setPrimaryDoctorCode(String primaryDoctorCode) {
        this.primaryDoctorCode = primaryDoctorCode;
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

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
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

    public Long getCurrentWardId() {
        return currentWardId;
    }

    public void setCurrentWardId(Long currentWardId) {
        this.currentWardId = currentWardId;
    }

    public String getCurrentWardName() {
        return currentWardName;
    }

    public void setCurrentWardName(String currentWardName) {
        this.currentWardName = currentWardName;
    }

    public String getCurrentRoomNumber() {
        return currentRoomNumber;
    }

    public void setCurrentRoomNumber(String currentRoomNumber) {
        this.currentRoomNumber = currentRoomNumber;
    }

    public Long getCurrentBedId() {
        return currentBedId;
    }

    public void setCurrentBedId(Long currentBedId) {
        this.currentBedId = currentBedId;
    }

    public String getCurrentBedNumber() {
        return currentBedNumber;
    }

    public void setCurrentBedNumber(String currentBedNumber) {
        this.currentBedNumber = currentBedNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
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
