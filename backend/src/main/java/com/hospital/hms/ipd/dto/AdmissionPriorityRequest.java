package com.hospital.hms.ipd.dto;

/**
 * Input for admission priority evaluation.
 * Used by AdmissionPriorityEvaluationService to determine final priority (P1–P4).
 */
public class AdmissionPriorityRequest {

    /** Admission source (e.g. OPD, EMERGENCY, REFERRAL). Case-insensitive match for EMERGENCY. */
    private String admissionSource;

    /** Ward type (e.g. GENERAL, ICU, EMERGENCY). Used for condition: ICU / Emergency. */
    private String wardType;

    /** True if patient is referred. Maps to REFERRED condition. */
    private boolean referred;

    /** Patient / admission details: special consideration flags. */
    private boolean seniorCitizen;
    private boolean pregnantWoman;
    private boolean child;
    private boolean disabledPatient;

    public AdmissionPriorityRequest() {
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

    public boolean isReferred() {
        return referred;
    }

    public void setReferred(boolean referred) {
        this.referred = referred;
    }

    public boolean isSeniorCitizen() {
        return seniorCitizen;
    }

    public void setSeniorCitizen(boolean seniorCitizen) {
        this.seniorCitizen = seniorCitizen;
    }

    public boolean isPregnantWoman() {
        return pregnantWoman;
    }

    public void setPregnantWoman(boolean pregnantWoman) {
        this.pregnantWoman = pregnantWoman;
    }

    public boolean isChild() {
        return child;
    }

    public void setChild(boolean child) {
        this.child = child;
    }

    public boolean isDisabledPatient() {
        return disabledPatient;
    }

    public void setDisabledPatient(boolean disabledPatient) {
        this.disabledPatient = disabledPatient;
    }
}
