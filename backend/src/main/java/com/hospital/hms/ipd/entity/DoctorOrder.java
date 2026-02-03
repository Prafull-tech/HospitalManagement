package com.hospital.hms.ipd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.doctor.entity.Doctor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Doctor order for an IPD admission. All activities linked with IPD Admission Number.
 * When executed (e.g. lab sent, drug dispensed), charge is posted to billing.
 */
@Entity
@Table(
    name = "doctor_orders",
    indexes = {
        @Index(name = "idx_doctor_order_admission", columnList = "ipd_admission_id"),
        @Index(name = "idx_doctor_order_ordered_at", columnList = "ordered_at")
    }
)
public class DoctorOrder extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipd_admission_id", nullable = false)
    private IPDAdmission ipdAdmission;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 30)
    private DoctorOrderType orderType;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull
    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Size(max = 30)
    @Column(name = "status", length = 30)
    private String status = "PENDING";

    public DoctorOrder() {
    }

    public IPDAdmission getIpdAdmission() { return ipdAdmission; }
    public void setIpdAdmission(IPDAdmission ipdAdmission) { this.ipdAdmission = ipdAdmission; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public DoctorOrderType getOrderType() { return orderType; }
    public void setOrderType(DoctorOrderType orderType) { this.orderType = orderType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public void setOrderedAt(LocalDateTime orderedAt) { this.orderedAt = orderedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
