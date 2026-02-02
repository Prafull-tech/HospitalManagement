package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.PriorityCode;

/**
 * Response DTO for POST /api/admission-priority/evaluate.
 * DB-agnostic.
 */
public class AdmissionPriorityEvaluateResponseDto {

    private PriorityCode priority;
    private String assignmentReason;

    public AdmissionPriorityEvaluateResponseDto() {
    }

    public AdmissionPriorityEvaluateResponseDto(PriorityCode priority, String assignmentReason) {
        this.priority = priority;
        this.assignmentReason = assignmentReason;
    }

    public PriorityCode getPriority() {
        return priority;
    }

    public void setPriority(PriorityCode priority) {
        this.priority = priority;
    }

    public String getAssignmentReason() {
        return assignmentReason;
    }

    public void setAssignmentReason(String assignmentReason) {
        this.assignmentReason = assignmentReason;
    }
}
