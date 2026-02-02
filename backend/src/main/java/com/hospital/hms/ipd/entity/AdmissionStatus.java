package com.hospital.hms.ipd.entity;

/**
 * IPD admission status lifecycle. DB-agnostic: stored as string.
 */
public enum AdmissionStatus {
    ADMITTED,
    TRANSFERRED,
    DISCHARGE_INITIATED,
    DISCHARGED,
    CANCELLED
}
