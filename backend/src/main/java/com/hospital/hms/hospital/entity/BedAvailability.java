package com.hospital.hms.hospital.entity;

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
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Hospital-wise bed availability. One record per Hospital + WardType.
 * Vacant = totalBeds - (occupiedBeds + reservedBeds + underCleaningBeds); not stored.
 * Spring Boot 3 / JPA, DB-agnostic.
 */
@Entity
@Table(
    name = "bed_availability",
    indexes = {
        @Index(name = "uk_bed_availability_hospital_ward", columnList = "hospital_id, ward_type", unique = true),
        @Index(name = "idx_bed_availability_hospital", columnList = "hospital_id")
    }
)
public class BedAvailability extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ward_type", nullable = false, length = 50)
    private WardType wardType;

    @NotNull
    @Min(0)
    @Column(name = "total_beds", nullable = false)
    private Integer totalBeds = 0;

    @NotNull
    @Min(0)
    @Column(name = "occupied_beds", nullable = false)
    private Integer occupiedBeds = 0;

    @NotNull
    @Min(0)
    @Column(name = "reserved_beds", nullable = false)
    private Integer reservedBeds = 0;

    @NotNull
    @Min(0)
    @Column(name = "under_cleaning_beds", nullable = false)
    private Integer underCleaningBeds = 0;

    /** User who last updated (username or user id). System logs on every update. */
    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    public BedAvailability() {
    }

    /**
     * Vacant beds: totalBeds - (occupiedBeds + reservedBeds + underCleaningBeds). Not persisted.
     */
    public int getVacant() {
        int used = occupiedBeds + reservedBeds + underCleaningBeds;
        return Math.max(0, totalBeds - used);
    }

    @AssertTrue(message = "occupiedBeds + reservedBeds + underCleaningBeds must not exceed totalBeds")
    public boolean isSumWithinTotal() {
        if (totalBeds == null || occupiedBeds == null || reservedBeds == null || underCleaningBeds == null) {
            return true;
        }
        long sum = (long) occupiedBeds + reservedBeds + underCleaningBeds;
        return sum <= totalBeds;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    public WardType getWardType() {
        return wardType;
    }

    public void setWardType(WardType wardType) {
        this.wardType = wardType;
    }

    public Integer getTotalBeds() {
        return totalBeds;
    }

    public void setTotalBeds(Integer totalBeds) {
        this.totalBeds = totalBeds;
    }

    public Integer getOccupiedBeds() {
        return occupiedBeds;
    }

    public void setOccupiedBeds(Integer occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
    }

    public Integer getReservedBeds() {
        return reservedBeds;
    }

    public void setReservedBeds(Integer reservedBeds) {
        this.reservedBeds = reservedBeds;
    }

    public Integer getUnderCleaningBeds() {
        return underCleaningBeds;
    }

    public void setUnderCleaningBeds(Integer underCleaningBeds) {
        this.underCleaningBeds = underCleaningBeds;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
