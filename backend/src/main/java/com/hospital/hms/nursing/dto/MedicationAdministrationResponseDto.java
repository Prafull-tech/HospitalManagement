package com.hospital.hms.nursing.dto;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Response DTO for medication administration (MAR).
 */
public class MedicationAdministrationResponseDto {

    private Long id;
    private Long ipdAdmissionId;
    private String medicationName;
    private String dosage;
    private String route;
    private LocalDateTime administeredAt;
    private Long administeredById;
    private String administeredByName;
    private String doctorOrderRef;
    private String remarks;
    private Instant createdAt;

    public MedicationAdministrationResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
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

    public Long getAdministeredById() {
        return administeredById;
    }

    public void setAdministeredById(Long administeredById) {
        this.administeredById = administeredById;
    }

    public String getAdministeredByName() {
        return administeredByName;
    }

    public void setAdministeredByName(String administeredByName) {
        this.administeredByName = administeredByName;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
