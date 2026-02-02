package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.AdmissionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for admitting a patient to IPD.
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

    private Long opdVisitId;

    @Size(max = 500)
    private String remarks;

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
}
