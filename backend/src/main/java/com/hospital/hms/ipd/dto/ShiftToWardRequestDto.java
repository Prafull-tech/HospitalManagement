package com.hospital.hms.ipd.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Request for POST /api/ipd/{admissionId}/shift-to-ward. Nursing staff performs shift; shift timestamp mandatory.
 */
public class ShiftToWardRequestDto {

    @NotNull(message = "Shift timestamp is required")
    private Instant shiftTimestamp;

    public ShiftToWardRequestDto() {
    }

    public Instant getShiftTimestamp() {
        return shiftTimestamp;
    }

    public void setShiftTimestamp(Instant shiftTimestamp) {
        this.shiftTimestamp = shiftTimestamp;
    }
}
