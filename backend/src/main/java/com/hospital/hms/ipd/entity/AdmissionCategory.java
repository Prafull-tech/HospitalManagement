package com.hospital.hms.ipd.entity;

/**
 * Admission category for priority master (Emergency/Critical, Serious, Stable, Elective).
 * Stored as string in DB (JPA @Enumerated(STRING)). DB-agnostic.
 */
public enum AdmissionCategory {
    EMERGENCY_CRITICAL,
    SERIOUS,
    STABLE,
    ELECTIVE
}
