package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.PriorityCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for POST /api/admission-priority/override.
 * Includes admissionId; only authority roles may call. Override requires reason; logged for audit.
 */
public class AdmissionPriorityOverrideApiRequestDto {

    @NotNull(message = "Admission ID is required")
    private Long admissionId;

    @NotNull(message = "New priority is required")
    private PriorityCode newPriority;

    @NotBlank(message = "Override reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;

    public AdmissionPriorityOverrideApiRequestDto() {
    }

    public Long getAdmissionId() {
        return admissionId;
    }

    public void setAdmissionId(Long admissionId) {
        this.admissionId = admissionId;
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
