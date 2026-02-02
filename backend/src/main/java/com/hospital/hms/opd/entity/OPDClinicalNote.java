package com.hospital.hms.opd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

/**
 * Clinical notes for OPD visit. One per visit (can be updated). DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(name = "opd_clinical_notes")
public class OPDClinicalNote extends BaseIdEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false, unique = true)
    private OPDVisit visit;

    @Size(max = 1000)
    @Column(name = "chief_complaint", length = 1000)
    private String chiefComplaint;

    @Size(max = 500)
    @Column(name = "provisional_diagnosis", length = 500)
    private String provisionalDiagnosis;

    @Size(max = 2000)
    @Column(name = "doctor_remarks", length = 2000)
    private String doctorRemarks;

    public OPDClinicalNote() {
    }

    public OPDVisit getVisit() {
        return visit;
    }

    public void setVisit(OPDVisit visit) {
        this.visit = visit;
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
