package com.hospital.hms.opd.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for adding/updating clinical notes.
 */
public class OPDClinicalNoteRequestDto {

    @Size(max = 1000)
    private String chiefComplaint;

    @Size(max = 500)
    private String provisionalDiagnosis;

    @Size(max = 2000)
    private String doctorRemarks;

    public OPDClinicalNoteRequestDto() {
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getProvisionalDiagnosis() {
        return provisionalDiagnosis;
    }

    public void setProvisionalDiagnosis(String provisionalDiagnosis) {
        this.provisionalDiagnosis = provisionalDiagnosis;
    }

    public String getDoctorRemarks() {
        return doctorRemarks;
    }

    public void setDoctorRemarks(String doctorRemarks) {
        this.doctorRemarks = doctorRemarks;
    }
}
