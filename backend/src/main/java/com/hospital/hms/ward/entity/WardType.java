package com.hospital.hms.ward.entity;

/**
 * Type of ward for Hospital-wise Bed Availability.
 * Stored as string in DB (JPA @Enumerated(STRING)). DB-agnostic.
 */
public enum WardType {
    GENERAL,
    SEMI_PRIVATE,
    PRIVATE,
    ICU,
    CCU,
    NICU,
    HDU,
    EMERGENCY
}
