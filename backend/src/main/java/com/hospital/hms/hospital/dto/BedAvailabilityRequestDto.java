package com.hospital.hms.hospital.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Create/update bed availability. Vacant is auto-calculated; do not set.
 * wardType: WardType enum name (e.g. GENERAL, ICU). Validation: occupiedBeds + reservedBeds + underCleaningBeds <= totalBeds (and entity @AssertTrue).
 */
public class BedAvailabilityRequestDto {

    @NotBlank(message = "Ward type is required (e.g. GENERAL, ICU)")
    private String wardType;

    @NotNull(message = "Total beds is required")
    @Min(value = 0, message = "Total beds must be >= 0")
    private Integer totalBeds = 0;

    @NotNull(message = "Occupied beds is required")
    @Min(value = 0, message = "Occupied beds must be >= 0")
    private Integer occupiedBeds = 0;

    @NotNull(message = "Reserved beds is required")
    @Min(value = 0, message = "Reserved beds must be >= 0")
    private Integer reservedBeds = 0;

    @NotNull(message = "Under cleaning beds is required")
    @Min(value = 0, message = "Under cleaning beds must be >= 0")
    private Integer underCleaningBeds = 0;

    public String getWardType() {
        return wardType;
    }

    public void setWardType(String wardType) {
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
}
