package com.hospital.hms.ipd.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for POST /api/admission-priority/evaluate.
 * All fields optional; evaluation uses defaults when omitted. DB-agnostic.
 */
public class AdmissionPriorityEvaluateRequestDto {

    /** Admission source (e.g. OPD, EMERGENCY, REFERRAL). Case-insensitive match for EMERGENCY. */
    @Size(max = 50)
    private String admissionSource;

    /** Ward type (e.g. GENERAL, ICU, EMERGENCY). Used for condition: ICU / Emergency. */
    @Size(max = 50)
    private String wardType;

    /** True if patient is referred. Maps to REFERRED condition. */
    private Boolean referred;

    /** Special consideration flags for priority boost. */
    private Boolean seniorCitizen;
    private Boolean pregnantWoman;
    private Boolean child;
    private Boolean disabledPatient;

    public AdmissionPriorityEvaluateRequestDto() {
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
