package com.hospital.hms.nursing.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.ipd.entity.IPDAdmission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Medication Administration Record (MAR). DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "medication_administrations",
    indexes = {
        @Index(name = "idx_mar_admission", columnList = "ipd_admission_id"),
        @Index(name = "idx_mar_administered_at", columnList = "administered_at")
    }
)
public class MedicationAdministration extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipd_admission_id", nullable = false)
    private IPDAdmission ipdAdmission;

    @NotBlank
    @Size(max = 255)
    @Column(name = "medication_name", nullable = false, length = 255)
    private String medicationName;

    @Size(max = 100)
    @Column(name = "dosage", length = 100)
    private String dosage;

    @Size(max = 50)
    @Column(name = "route", length = 50)
    private String route;

    @NotNull
    @Column(name = "administered_at", nullable = false)
    private LocalDateTime administeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administered_by_id")
    private NursingStaff administeredBy;

    @Size(max = 100)
    @Column(name = "doctor_order_ref", length = 100)
    private String doctorOrderRef;

    @Size(max = 500)
    @Column(name = "remarks", length = 500)
    private String remarks;

    public MedicationAdministration() {
    }

    public IPDAdmission getIpdAdmission() {
        return ipdAdmission;
    }

    public void setIpdAdmission(IPDAdmission ipdAdmission) {
        this.ipdAdmission = ipdAdmission;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public LocalDateTime getAdministeredAt() {
        return administeredAt;
    }

    public void setAdministeredAt(LocalDateTime administeredAt) {
        this.administeredAt = administeredAt;
    }

    public NursingStaff getAdministeredBy() {
        return administeredBy;
    }

    public void setAdministeredBy(NursingStaff administeredBy) {
        this.administeredBy = administeredBy;
    }

    public String getDoctorOrderRef() {
        return doctorOrderRef;
    }

    public void setDoctorOrderRef(String doctorOrderRef) {
        this.doctorOrderRef = doctorOrderRef;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
