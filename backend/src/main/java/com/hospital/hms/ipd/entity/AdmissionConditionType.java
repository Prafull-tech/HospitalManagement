package com.hospital.hms.ipd.entity;

/**
 * Admission condition type for priority rules. One rule per condition.
 * Stored as string in DB (JPA @Enumerated(STRING)). DB-agnostic.
 * <ul>
 *   <li>EMERGENCY → P1</li>
 *   <li>ICU → P2</li>
 *   <li>REFERRED → P3</li>
 *   <li>ELECTIVE → P4</li>
 * </ul>
 */
public enum AdmissionConditionType {
    EMERGENCY,
    ICU,
    REFERRED,
    ELECTIVE
}
