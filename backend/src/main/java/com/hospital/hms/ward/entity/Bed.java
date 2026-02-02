package com.hospital.hms.ward.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Bed within a ward (optionally within a room). DB-agnostic (H2 & MySQL).
 * Unique bed number per ward. IPD BedAllocation references this Bed.
 */
@Entity
@Table(
    name = "beds",
    indexes = {
        @Index(name = "idx_bed_ward", columnList = "ward_id"),
        @Index(name = "idx_bed_status", columnList = "bed_status"),
        @Index(name = "idx_bed_ward_number", columnList = "ward_id, bed_number", unique = true)
    }
)
public class Bed extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @NotBlank
    @Size(max = 30)
    @Column(name = "bed_number", nullable = false, length = 30)
    private String bedNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "bed_status", nullable = false, length = 20)
    private BedStatus bedStatus = BedStatus.AVAILABLE;

    @Column(name = "is_isolation", nullable = false)
    private Boolean isIsolation = false;

    @Column(name = "equipment_ready", nullable = false)
    private Boolean equipmentReady = true;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Bed() {
    }

    public Ward getWard() {
        return ward;
    }

    public void setWard(Ward ward) {
        this.ward = ward;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public BedStatus getBedStatus() {
        return bedStatus;
    }

    public void setBedStatus(BedStatus bedStatus) {
        this.bedStatus = bedStatus;
    }

    public Boolean getIsIsolation() {
        return isIsolation;
    }

    public void setIsIsolation(Boolean isIsolation) {
        this.isIsolation = isIsolation;
    }

    public Boolean getEquipmentReady() {
        return equipmentReady;
    }

    public void setEquipmentReady(Boolean equipmentReady) {
        this.equipmentReady = equipmentReady;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
