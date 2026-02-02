package com.hospital.hms.opd.dto;

import com.hospital.hms.opd.entity.VisitStatus;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating visit status.
 */
public class OPDStatusRequestDto {

    @NotNull(message = "Status is required")
    private VisitStatus status;

    public OPDStatusRequestDto() {
    }

    public VisitStatus getStatus() {
        return status;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }
}
