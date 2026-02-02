package com.hospital.hms.ipd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.ward.entity.WardType;
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

import java.time.Instant;

/**
 * Transfer execution workflow. Tracks patient transfer from recommendation through completion.
 * Workflow: RECOMMENDED → CONSENTED → BED_RESERVED → IN_TRANSIT → COMPLETED (or CANCELLED).
 * DB-agnostic JPA design (H2 & MySQL).
 */
@Entity
@Table(
    name = "patient_transfer",
    indexes = {
        @Index(name = "idx_patient_transfer_admission", columnList = "ipd_admission_id"),
        @Index(name = "idx_patient_transfer_status", columnList = "transfer_status"),
        @Index(name = "idx_patient_transfer_type", columnList = "transfer_type"),
        @Index(name = "idx_patient_transfer_time", columnList = "transfer_time")
    }
)
public class PatientTransfer extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipd_admission_id", nullable = false)
    private IPDAdmission ipdAdmission;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "from_ward_type", nullable = false, length = 30)
    private WardType fromWardType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "to_ward_type", nullable = false, length = 30)
    private WardType toWardType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", nullable = false, length = 20)
    private TransferType transferType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_status", nullable = false, length = 20)
    private TransferStatus transferStatus;

    /** Nurse assigned to transfer. FK to nursing staff; nullable until assigned. */
    @Column(name = "nurse_id")
    private Long nurseId;

    /** Attendant assigned to transfer. FK to staff; nullable until assigned. */
    @Column(name = "attendant_id")
    private Long attendantId;

    /** Equipment used during transfer (e.g. OXYGEN, MONITOR). Nullable if none. */
    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_used", length = 30)
    private EquipmentType equipmentUsed;

    /** When transfer was executed (e.g. IN_TRANSIT or COMPLETED time). Nullable until in progress. */
    @Column(name = "transfer_time")
    private Instant transferTime;

    public PatientTransfer() {
    }

    public IPDAdmission getIpdAdmission() {
        return ipdAdmission;
    }

    public void setIpdAdmission(IPDAdmission ipdAdmission) {
        this.ipdAdmission = ipdAdmission;
    }

    public WardType getFromWardType() {
        return fromWardType;
    }

    public void setFromWardType(WardType fromWardType) {
        this.fromWardType = fromWardType;
    }

    public WardType getToWardType() {
        return toWardType;
    }

    public void setToWardType(WardType toWardType) {
        this.toWardType = toWardType;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
    }

    public TransferStatus getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(TransferStatus transferStatus) {
        this.transferStatus = transferStatus;
    }

    public Long getNurseId() {
        return nurseId;
    }

    public void setNurseId(Long nurseId) {
        this.nurseId = nurseId;
    }

    public Long getAttendantId() {
        return attendantId;
    }

    public void setAttendantId(Long attendantId) {
        this.attendantId = attendantId;
    }

    public EquipmentType getEquipmentUsed() {
        return equipmentUsed;
    }

    public void setEquipmentUsed(EquipmentType equipmentUsed) {
        this.equipmentUsed = equipmentUsed;
    }

    public Instant getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(Instant transferTime) {
        this.transferTime = transferTime;
    }
}
