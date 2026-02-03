package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.AdmissionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for admitting a patient to IPD (POST /api/ipd/admit or /api/ipd/admissions).
 */
public class IPDAdmissionRequestDto {

    @NotNull(message = "Patient UHID is required")
    @Size(max = 50)
    private String patientUhid;

    @NotNull(message = "Primary doctor ID is required")
    private Long primaryDoctorId;

    @NotNull(message = "Admission type is required")
    private AdmissionType admissionType;

    @NotNull(message = "Bed ID is required")
    private Long bedId;

    /** Required for IPD Admit form; defaults to now if null (e.g. legacy POST /admissions). */
    private LocalDateTime admissionDateTime;

    /** Required for IPD Admit form; max 1000 chars. */
    @Size(max = 1000)
    private String diagnosis;

    private Long opdVisitId;

    @Size(max = 500)
    private String remarks;

    private BigDecimal depositAmount;

    @Size(max = 255)
    private String insuranceTpa;

    @Size(max = 500)
    private String admissionFormDocumentRef;

    @Size(max = 500)
    private String consentFormDocumentRef;

    @Size(max = 500)
    private String idProofDocumentRef;

    /** Optional: admission source for priority (e.g. OPD, EMERGENCY, REFERRAL). Ward type is derived from bed if not set. */
    @Size(max = 50)
    private String admissionSource;

    /** Optional: ward type for priority (e.g. GENERAL, ICU). Overrides ward type from bed when set. */
    @Size(max = 50)
    private String wardType;

    /** Optional: true if patient is referred. Used for priority evaluation. */
    private Boolean referred;

    /** Optional: special consideration flags for priority boost. */
    private Boolean seniorCitizen;
    private Boolean pregnantWoman;
    private Boolean child;
    private Boolean disabledPatient;

    public IPDAdmissionRequestDto() {
    }

    public String getPatientUhid() {
        return patientUhid;
    }

    public void setPatientUhid(String patientUhid) {
        this.patientUhid = patientUhid;
    }

    public Long getPrimaryDoctorId() {
        return primaryDoctorId;
    }

    public void setPrimaryDoctorId(Long primaryDoctorId) {
        this.primaryDoctorId = primaryDoctorId;
    }

    public AdmissionType getAdmissionType() {
        return admissionType;
    }

    public void setAdmissionType(AdmissionType admissionType) {
        this.admissionType = admissionType;
    }

    public Long getBedId() {
        return bedId;
    }

    public void setBedId(Long bedId) {
        this.bedId = bedId;
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

    public String getAdmissionSource() {
        return admissionSource;
    }

    public void setAdmissionSource(String admissionSource) {
        this.admissionSource = admissionSource;
    }

    public String getWardType() {
        return wardType;
    }

    public void setWardType(String wardType) {
        this.wardType = wardType;
    }

    public Boolean getReferred() {
        return referred;
    }

    public void setReferred(Boolean referred) {
        this.referred = referred;
    }

    public Boolean getSeniorCitizen() {
        return seniorCitizen;
    }

    public void setSeniorCitizen(Boolean seniorCitizen) {
        this.seniorCitizen = seniorCitizen;
    }

    public Boolean getPregnantWoman() {
        return pregnantWoman;
    }

    public void setPregnantWoman(Boolean pregnantWoman) {
        this.pregnantWoman = pregnantWoman;
    }

    public Boolean getChild() {
        return child;
    }

    public void setChild(Boolean child) {
        this.child = child;
    }

    public Boolean getDisabledPatient() {
        return disabledPatient;
    }

    public void setDisabledPatient(Boolean disabledPatient) {
        this.disabledPatient = disabledPatient;
    }

    public LocalDateTime getAdmissionDateTime() {
        return admissionDateTime;
    }

    public void setAdmissionDateTime(LocalDateTime admissionDateTime) {
        this.admissionDateTime = admissionDateTime;
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
}
