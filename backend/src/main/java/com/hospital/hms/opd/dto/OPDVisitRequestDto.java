package com.hospital.hms.opd.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request DTO for registering OPD visit.
 */
public class OPDVisitRequestDto {

    @NotNull(message = "Patient UHID is required")
    @Size(max = 50)
    private String patientUhid;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    /** Optional; defaults to today when null. */
    private LocalDate visitDate;

    public OPDVisitRequestDto() {
    }

    public String getPatientUhid() {
        return patientUhid;
    }

    public void setPatientUhid(String patientUhid) {
        this.patientUhid = patientUhid;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }
}
