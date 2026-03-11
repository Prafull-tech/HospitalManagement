package com.hospital.hms.lab.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.hospital.hms.lab.entity.LabOrderPriority;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request for creating a lab order (OPD, IPD, or Emergency).
 */
public class LabOrderRequestDto {

    @NotNull(message = "Doctor ID is required")
    @JsonAlias("doctorId")
    private Long orderedByDoctorId;

    private Long patientId;      // Optional if ipdAdmissionId or opdVisitId provided
    private Long ipdAdmissionId; // For IPD
    private Long opdVisitId;     // For OPD

    private LabOrderPriority priority = LabOrderPriority.NORMAL;
    private Boolean isPriority;  // Backward compat: true -> EMERGENCY

    private List<Long> testIds;  // Multiple tests
    @JsonAlias("testId")
    private Long testMasterId;  // Single test (backward compat - used when testIds not provided)

    public LabOrderRequestDto() {
    }

    public Long getOrderedByDoctorId() {
        return orderedByDoctorId;
    }

    public void setOrderedByDoctorId(Long orderedByDoctorId) {
        this.orderedByDoctorId = orderedByDoctorId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
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

    public LabOrderPriority getPriority() {
        return priority;
    }

    public void setPriority(LabOrderPriority priority) {
        this.priority = priority;
    }

    public Boolean getIsPriority() {
        return isPriority;
    }

    public void setIsPriority(Boolean isPriority) {
        this.isPriority = isPriority;
    }

    public List<Long> getTestIds() {
        return testIds;
    }

    public void setTestIds(List<Long> testIds) {
        this.testIds = testIds;
    }

    public Long getTestMasterId() {
        return testMasterId;
    }

    public void setTestMasterId(Long testMasterId) {
        this.testMasterId = testMasterId;
    }
}
