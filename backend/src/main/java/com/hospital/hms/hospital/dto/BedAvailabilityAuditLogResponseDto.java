package com.hospital.hms.hospital.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.Instant;

/**
 * Read-only audit trail entry. Exposed by GET .../bed-availability/{id}/audit.
 */
@JsonPropertyOrder({ "id", "bedAvailabilityId", "changedAt", "changedBy", "performedByRole", "action" })
public class BedAvailabilityAuditLogResponseDto {

    private Long id;
    private Long bedAvailabilityId;
    private Instant changedAt;
    private String changedBy;
    private String performedByRole;
    private String action;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBedAvailabilityId() {
        return bedAvailabilityId;
    }

    public void setBedAvailabilityId(Long bedAvailabilityId) {
        this.bedAvailabilityId = bedAvailabilityId;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getPerformedByRole() {
        return performedByRole;
    }

    public void setPerformedByRole(String performedByRole) {
        this.performedByRole = performedByRole;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
