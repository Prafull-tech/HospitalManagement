package com.hospital.hms.laundry.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Linen inventory record for ward issue/return tracking.
 * Tracks linen flow: issued to ward, returned to laundry, status through wash cycle.
 */
@Entity
@Table(
    name = "linen_inventory",
    indexes = {
        @Index(name = "idx_linen_ward", columnList = "ward_name"),
        @Index(name = "idx_linen_status", columnList = "laundry_status"),
        @Index(name = "idx_linen_created", columnList = "created_at")
    }
)
public class LinenInventory extends BaseIdEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "linen_type", nullable = false, length = 30)
    private LinenType linenType;

    @Column(name = "ward_name", nullable = false, length = 100)
    private String wardName;

    @Column(name = "quantity_issued", nullable = false)
    private int quantityIssued = 0;

    @Column(name = "quantity_returned", nullable = false)
    private int quantityReturned = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "laundry_status", nullable = false, length = 20)
    private LaundryStatus laundryStatus = LaundryStatus.DIRTY;

    @Column(name = "ipd_admission_id")
    private Long ipdAdmissionId;

    public LinenInventory() {
    }

    public LinenType getLinenType() {
        return linenType;
    }

    public void setLinenType(LinenType linenType) {
        this.linenType = linenType;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public int getQuantityIssued() {
        return quantityIssued;
    }

    public void setQuantityIssued(int quantityIssued) {
        this.quantityIssued = quantityIssued;
    }

    public int getQuantityReturned() {
        return quantityReturned;
    }

    public void setQuantityReturned(int quantityReturned) {
        this.quantityReturned = quantityReturned;
    }

    public LaundryStatus getLaundryStatus() {
        return laundryStatus;
    }

    public void setLaundryStatus(LaundryStatus laundryStatus) {
        this.laundryStatus = laundryStatus;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }
}
