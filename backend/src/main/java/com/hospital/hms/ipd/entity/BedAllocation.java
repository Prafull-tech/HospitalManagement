package com.hospital.hms.ipd.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.ward.entity.Bed;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Bed allocation for an admission. References ward.Bed. One active allocation per bed (releasedAt is null).
 * DB-agnostic (H2 & MySQL).
 */
@Entity
@Table(
    name = "ipd_bed_allocations",
    indexes = {
        @Index(name = "idx_bed_allocation_bed", columnList = "bed_id"),
        @Index(name = "idx_bed_allocation_admission", columnList = "admission_id")
    }
)
public class BedAllocation extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_id", nullable = false)
    private Bed bed;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id", nullable = false)
    private IPDAdmission admission;

    @NotNull
    @Column(name = "allocated_at", nullable = false, updatable = false)
    private Instant allocatedAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    public BedAllocation() {
    }

    public Bed getBed() {
        return bed;
    }

    public void setBed(Bed bed) {
        this.bed = bed;
    }

    public IPDAdmission getAdmission() {
        return admission;
    }

    public void setAdmission(IPDAdmission admission) {
        this.admission = admission;
    }

    public Instant getAllocatedAt() {
        return allocatedAt;
    }

    public void setAllocatedAt(Instant allocatedAt) {
        this.allocatedAt = allocatedAt;
    }

    public Instant getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(Instant releasedAt) {
        this.releasedAt = releasedAt;
    }
}
