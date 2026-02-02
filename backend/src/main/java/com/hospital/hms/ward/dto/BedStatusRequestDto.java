package com.hospital.hms.ward.dto;

import com.hospital.hms.ward.entity.BedStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating bed status.
 */
public class BedStatusRequestDto {

    @NotNull(message = "Bed status is required")
    private BedStatus bedStatus;

    public BedStatusRequestDto() {
    }

    public BedStatus getBedStatus() {
        return bedStatus;
    }

    public void setBedStatus(BedStatus bedStatus) {
        this.bedStatus = bedStatus;
    }
}
