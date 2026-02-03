package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.AdmissionStatus;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTOs for IPD Admission Status Master API.
 */
public final class AdmissionStatusMasterDto {

    /** Single status for list response (code + display name). SHIFTED = display for TRANSFERRED. */
    public static class StatusItem {
        private String code;
        private String displayName;

        public static StatusItem of(AdmissionStatus status) {
            StatusItem item = new StatusItem();
            item.setCode(status.name());
            item.setDisplayName(displayNameFor(status));
            return item;
        }

        /** Display name for API/audit: SHIFTED for TRANSFERRED, otherwise enum name with spaces. */
        public static String displayNameFor(AdmissionStatus status) {
            if (status == AdmissionStatus.TRANSFERRED) {
                return "SHIFTED";
            }
            return status.name().replace('_', ' ');
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }

    /** Allowed transitions from a status. */
    public static class AllowedTransitionsResponse {
        private String fromStatus;
        private Set<StatusItem> allowedToStatuses;

        public String getFromStatus() {
            return fromStatus;
        }

        public void setFromStatus(String fromStatus) {
            this.fromStatus = fromStatus;
        }

        public Set<StatusItem> getAllowedToStatuses() {
            return allowedToStatuses;
        }

        public void setAllowedToStatuses(Set<StatusItem> allowedToStatuses) {
            this.allowedToStatuses = allowedToStatuses;
        }

        public static AllowedTransitionsResponse of(AdmissionStatus from, Set<AdmissionStatus> allowed) {
            AllowedTransitionsResponse r = new AllowedTransitionsResponse();
            r.setFromStatus(from == null ? "null" : from.name());
            r.setAllowedToStatuses(allowed.stream().map(StatusItem::of).collect(Collectors.toSet()));
            return r;
        }
    }

    /** Request to change status (direct API). */
    public static class ChangeStatusRequest {
        @NotNull(message = "toStatus is required")
        private AdmissionStatus toStatus;
        private String reason;

        public AdmissionStatus getToStatus() {
            return toStatus;
        }

        public void setToStatus(AdmissionStatus toStatus) {
            this.toStatus = toStatus;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    /** Single audit log entry. fromStatus/toStatus are enum names; display names use SHIFTED for TRANSFERRED. */
    public static class AuditLogItem {
        private Long admissionId;
        private String fromStatus;
        private String toStatus;
        private String fromStatusDisplay;
        private String toStatusDisplay;
        private java.time.Instant changedAt;
        private String changedBy;
        private String reason;

        public Long getAdmissionId() {
            return admissionId;
        }

        public void setAdmissionId(Long admissionId) {
            this.admissionId = admissionId;
        }

        public String getFromStatus() {
            return fromStatus;
        }

        public void setFromStatus(String fromStatus) {
            this.fromStatus = fromStatus;
        }

        public String getToStatus() {
            return toStatus;
        }

        public void setToStatus(String toStatus) {
            this.toStatus = toStatus;
        }

        public String getFromStatusDisplay() {
            return fromStatusDisplay;
        }

        public void setFromStatusDisplay(String fromStatusDisplay) {
            this.fromStatusDisplay = fromStatusDisplay;
        }

        public String getToStatusDisplay() {
            return toStatusDisplay;
        }

        public void setToStatusDisplay(String toStatusDisplay) {
            this.toStatusDisplay = toStatusDisplay;
        }

        public java.time.Instant getChangedAt() {
            return changedAt;
        }

        public void setChangedAt(java.time.Instant changedAt) {
            this.changedAt = changedAt;
        }

        public String getChangedBy() {
            return changedBy;
        }

        public void setChangedBy(String changedBy) {
            this.changedBy = changedBy;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    private AdmissionStatusMasterDto() {
    }
}
