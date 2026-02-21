package com.hospital.hms.pharmacy.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.reception.entity.Patient;
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
 * Medication order from doctor (IPD, OPD, Emergency). Populates pharmacy issue queue.
 * NABH / Medication Safety compliant.
 */
@Entity
@Table(
    name = "medication_orders",
    indexes = {
        @Index(name = "idx_med_order_status", columnList = "status"),
        @Index(name = "idx_med_order_patient", columnList = "patient_id"),
        @Index(name = "idx_med_order_ordered_at", columnList = "ordered_at"),
        @Index(name = "idx_med_order_ward_priority", columnList = "ward_type, priority")
    }
)
public class MedicationOrder extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Size(max = 50)
    @Column(name = "uhid", length = 50)
    private String uhid;

    @Column(name = "ipd_admission_id")
    private Long ipdAdmissionId;

    @Column(name = "opd_visit_id")
    private Long opdVisitId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ward_type", nullable = false, length = 30)
    private MedicationOrderWardType wardType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private MedicineMaster medicine;

    @Size(max = 100)
    @Column(name = "dosage", length = 100)
    private String dosage;

    @Size(max = 100)
    @Column(name = "frequency", length = 100)
    private String frequency;

    @Size(max = 50)
    @Column(name = "route", length = 50)
    private String route;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private MedicationOrderPriority priority = MedicationOrderPriority.NORMAL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MedicationOrderStatus status = MedicationOrderStatus.PENDING;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordered_by_doctor_id", nullable = false)
    private Doctor orderedByDoctor;

    @NotNull
    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Size(max = 255)
    @Column(name = "issued_by", length = 255)
    private String issuedBy;

    @Size(max = 100)
    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    public MedicationOrder() {
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getUhid() {
        return uhid;
    }

    public void setUhid(String uhid) {
        this.uhid = uhid;
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

    public MedicationOrderWardType getWardType() {
        return wardType;
    }

    public void setWardType(MedicationOrderWardType wardType) {
        this.wardType = wardType;
    }

    public MedicineMaster getMedicine() {
        return medicine;
    }

    public void setMedicine(MedicineMaster medicine) {
        this.medicine = medicine;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public MedicationOrderPriority getPriority() {
        return priority;
    }

    public void setPriority(MedicationOrderPriority priority) {
        this.priority = priority;
    }

    public MedicationOrderStatus getStatus() {
        return status;
    }

    public void setStatus(MedicationOrderStatus status) {
        this.status = status;
    }

    public Doctor getOrderedByDoctor() {
        return orderedByDoctor;
    }

    public void setOrderedByDoctor(Doctor orderedByDoctor) {
        this.orderedByDoctor = orderedByDoctor;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }
}
