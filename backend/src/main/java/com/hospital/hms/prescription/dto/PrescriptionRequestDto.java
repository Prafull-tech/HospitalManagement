package com.hospital.hms.prescription.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionRequestDto {

    private Long opdVisitId;
    private Long ipdAdmissionId;
    private Long doctorId;

    @NotBlank(message = "Patient UHID is required")
    private String patientUhid;

    private String notes;
    private LocalDate followUpDate;

    @Valid
    @NotEmpty(message = "At least one prescription item is required")
    private List<PrescriptionItemRequestDto> items = new ArrayList<>();

    public Long getOpdVisitId() {
        return opdVisitId;
    }

    public void setOpdVisitId(Long opdVisitId) {
        this.opdVisitId = opdVisitId;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientUhid() {
        return patientUhid;
    }

    public void setPatientUhid(String patientUhid) {
        this.patientUhid = patientUhid;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(LocalDate followUpDate) {
        this.followUpDate = followUpDate;
    }

    public List<PrescriptionItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemRequestDto> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}