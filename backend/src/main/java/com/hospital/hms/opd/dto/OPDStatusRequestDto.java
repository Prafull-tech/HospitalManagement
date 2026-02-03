package com.hospital.hms.opd.dto;

import com.hospital.hms.opd.entity.ConsultationOutcome;
import com.hospital.hms.opd.entity.VisitStatus;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating visit status. Optional consultationOutcome for doctor result.
 */
public class OPDStatusRequestDto {

    @NotNull(message = "Status is required")
    private VisitStatus status;

    /** Doctor consultation result: OPD treatment only, Lab test advised, or IPD admission advised. */
    private ConsultationOutcome consultationOutcome;

    public OPDStatusRequestDto() {
    }

    public VisitStatus getStatus() {
        return status;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    public ConsultationOutcome getConsultationOutcome() {
        return consultationOutcome;
    }

    public void setConsultationOutcome(ConsultationOutcome consultationOutcome) {
        this.consultationOutcome = consultationOutcome;
    }
}
