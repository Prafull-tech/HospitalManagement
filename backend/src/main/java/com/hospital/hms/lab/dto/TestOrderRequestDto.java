package com.hospital.hms.lab.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a test order (OPD or IPD).
 */
public class TestOrderRequestDto {

    @NotNull(message = "Test master ID is required")
    private Long testMasterId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    private Long ipdAdmissionId; // For IPD orders
    private Long opdVisitId; // For OPD orders

    @Size(max = 1000)
    private String clinicalNotes;

    private Boolean isPriority = false; // Emergency / ICU tests

    public TestOrderRequestDto() {
    }

    public Long getTestMasterId() {
        return testMasterId;
    }

    public void setTestMasterId(Long testMasterId) {
        this.testMasterId = testMasterId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }

    public Long getOpdVisitId() {
        return opdVisitId;
    }

    public void setOpdVisitId(Long opdVisitId) {
        this.opdVisitId = opdVisitId;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public Boolean getIsPriority() {
        return isPriority;
    }

    public void setIsPriority(Boolean isPriority) {
        this.isPriority = isPriority;
    }
}
