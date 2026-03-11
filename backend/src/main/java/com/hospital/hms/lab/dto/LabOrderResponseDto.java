package com.hospital.hms.lab.dto;

import com.hospital.hms.lab.entity.LabOrderPriority;
import com.hospital.hms.lab.entity.LabOrderStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for lab order.
 */
public class LabOrderResponseDto {

    private Long id;
    private Long patientId;
    private String uhid;
    private String patientName;
    private Long ipdAdmissionId;
    private String ipdAdmissionNumber;
    private Long opdVisitId;
    private String opdVisitNumber;
    private Long orderedByDoctorId;
    private String orderedByDoctorName;
    private LabOrderPriority priority;
    private LabOrderStatus status;
    private LocalDateTime orderedAt;
    private List<LabOrderItemResponseDto> items;

    public LabOrderResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getUhid() {
        return uhid;
    }

    public void setUhid(String uhid) {
        this.uhid = uhid;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }

    public String getIpdAdmissionNumber() {
        return ipdAdmissionNumber;
    }

    public void setIpdAdmissionNumber(String ipdAdmissionNumber) {
        this.ipdAdmissionNumber = ipdAdmissionNumber;
    }

    public Long getOpdVisitId() {
        return opdVisitId;
    }

    public void setOpdVisitId(Long opdVisitId) {
        this.opdVisitId = opdVisitId;
    }

    public String getOpdVisitNumber() {
        return opdVisitNumber;
    }

    public void setOpdVisitNumber(String opdVisitNumber) {
        this.opdVisitNumber = opdVisitNumber;
    }

    public Long getOrderedByDoctorId() {
        return orderedByDoctorId;
    }

    public void setOrderedByDoctorId(Long orderedByDoctorId) {
        this.orderedByDoctorId = orderedByDoctorId;
    }

    public String getOrderedByDoctorName() {
        return orderedByDoctorName;
    }

    public void setOrderedByDoctorName(String orderedByDoctorName) {
        this.orderedByDoctorName = orderedByDoctorName;
    }

    public LabOrderPriority getPriority() {
        return priority;
    }

    public void setPriority(LabOrderPriority priority) {
        this.priority = priority;
    }

    public LabOrderStatus getStatus() {
        return status;
    }

    public void setStatus(LabOrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }

    public List<LabOrderItemResponseDto> getItems() {
        return items;
    }

    public void setItems(List<LabOrderItemResponseDto> items) {
        this.items = items;
    }
}
