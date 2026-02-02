package com.hospital.hms.ipd.dto;

import com.hospital.hms.ipd.entity.AdmissionConditionType;
import com.hospital.hms.ipd.entity.PriorityCode;

import java.util.Collections;
import java.util.List;

/**
 * Result of admission priority evaluation: assigned priority, reason, rule and special consideration for audit.
 */
public class AdmissionPriorityResult {

    private final PriorityCode priority;
    private final String assignmentReason;
    private final AdmissionConditionType ruleApplied;
    private final List<String> specialConsiderationApplied;

    public AdmissionPriorityResult(PriorityCode priority, String assignmentReason,
                                   AdmissionConditionType ruleApplied, List<String> specialConsiderationApplied) {
        this.priority = priority;
        this.assignmentReason = assignmentReason != null ? assignmentReason : "";
        this.ruleApplied = ruleApplied;
        this.specialConsiderationApplied = specialConsiderationApplied != null
                ? List.copyOf(specialConsiderationApplied) : Collections.emptyList();
    }

    public PriorityCode getPriority() {
        return priority;
    }

    public String getAssignmentReason() {
        return assignmentReason;
    }

    /** Rule/condition applied (EMERGENCY, ICU, REFERRED, ELECTIVE). */
    public AdmissionConditionType getRuleApplied() {
        return ruleApplied;
    }

    /** Special consideration types applied (e.g. SENIOR_CITIZEN, CHILD). */
    public List<String> getSpecialConsiderationApplied() {
        return specialConsiderationApplied;
    }
}
