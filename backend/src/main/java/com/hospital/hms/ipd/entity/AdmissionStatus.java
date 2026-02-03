package com.hospital.hms.ipd.entity;

/**
 * IPD admission status lifecycle. DB-agnostic: stored as string.
 * ADMITTED = paperwork done, bed RESERVED; ACTIVE = shifted to ward, bed OCCUPIED.
 */
public enum AdmissionStatus {
    ADMITTED,
    ACTIVE,
    TRANSFERRED,
    DISCHARGE_INITIATED,
    DISCHARGED,
    CANCELLED,
    REFERRED,
    LAMA,
    EXPIRED
}
