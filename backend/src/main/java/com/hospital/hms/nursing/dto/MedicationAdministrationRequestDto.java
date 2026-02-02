package com.hospital.hms.nursing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request DTO for medication administration (MAR).
 */
public class MedicationAdministrationRequestDto {

    @NotNull(message = "IPD admission ID is required")
    private Long ipdAdmissionId;

    @NotBlank(message = "Medication name is required")
    @Size(max = 255)
    private String medicationName;

    @Size(max = 100)
    private String dosage;

    @Size(max = 50)
    private String route;

    private LocalDateTime administeredAt;

    private Long administeredById;

    @Size(max = 100)
    private String doctorOrderRef;

    @Size(max = 500)
    private String remarks;

    public MedicationAdministrationRequestDto() {
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
