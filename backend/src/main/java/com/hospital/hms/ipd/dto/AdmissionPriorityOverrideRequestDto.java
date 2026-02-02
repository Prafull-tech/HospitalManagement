package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.PriorityCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for overriding admission priority.
 * Only authority roles (MEDICAL_SUPERINTENDENT, EMERGENCY_HEAD, IPD_MANAGER) may use this.
 * Override requires a reason and is logged for audit.
 */
public class AdmissionPriorityOverrideRequestDto {

    @NotNull(message = "New priority is required")
    private PriorityCode newPriority;

    @NotBlank(message = "Override reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;

    public AdmissionPriorityOverrideRequestDto() {
    }

    public PriorityCode getNewPriority() {
        return newPriority;
    }

    public void setNewPriority(PriorityCode newPriority) {
        this.newPriority = newPriority;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
