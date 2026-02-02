package com.hospital.hms.ward.entity;

/**
 * Bed status. DB-agnostic: stored as string.
 * Aligns with NABH / Bed Availability SOP: Vacant, Occupied, Reserved, Cleaning, Maintenance, Isolation.
 */
public enum BedStatus {
    /** Bed is free (displayed as Vacant in UI). */
    AVAILABLE,
    /** Patient admitted. */
    OCCUPIED,
    /** Blocked for surgery/emergency. */
    RESERVED,
    /** Under housekeeping. */
    CLEANING,
    /** Under repair. */
    MAINTENANCE,
    /** Infection control. */
    ISOLATION
}
