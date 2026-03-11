package com.hospital.hms.lab.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.opd.entity.OPDVisit;
import com.hospital.hms.reception.entity.Patient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lab order header. Can be from OPD, IPD, or Emergency.
 */
@Entity
@Table(
    name = "lab_orders",
    indexes = {
        @Index(name = "idx_lab_order_patient", columnList = "patient_id"),
        @Index(name = "idx_lab_order_ipd", columnList = "ipd_admission_id"),
        @Index(name = "idx_lab_order_opd", columnList = "opd_visit_id"),
        @Index(name = "idx_lab_order_status", columnList = "status"),
        @Index(name = "idx_lab_order_ordered_at", columnList = "ordered_at")
    }
)
public class LabOrder extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotBlank
    @Size(max = 50)
    @Column(name = "uhid", nullable = false, length = 50)
    private String uhid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipd_admission_id")
    private IPDAdmission ipdAdmission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opd_visit_id")
    private OPDVisit opdVisit;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordered_by_doctor_id", nullable = false)
    private Doctor orderedByDoctor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private LabOrderPriority priority = LabOrderPriority.NORMAL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LabOrderStatus status = LabOrderStatus.ORDERED;

    @NotNull
    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabOrderItem> items = new ArrayList<>();

    public LabOrder() {
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

    public IPDAdmission getIpdAdmission() {
        return ipdAdmission;
    }

    public void setIpdAdmission(IPDAdmission ipdAdmission) {
        this.ipdAdmission = ipdAdmission;
    }

    public OPDVisit getOpdVisit() {
        return opdVisit;
    }

    public void setOpdVisit(OPDVisit opdVisit) {
        this.opdVisit = opdVisit;
    }

    public Doctor getOrderedByDoctor() {
        return orderedByDoctor;
    }

    public void setOrderedByDoctor(Doctor orderedByDoctor) {
        this.orderedByDoctor = orderedByDoctor;
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

    public List<LabOrderItem> getItems() {
        return items;
    }

    public void setItems(List<LabOrderItem> items) {
        this.items = items;
    }
}
