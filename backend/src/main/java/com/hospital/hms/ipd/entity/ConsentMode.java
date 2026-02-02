package com.hospital.hms.ipd.entity;

/**
 * Mode of family consent for patient transfers.
 * Stored as string in DB (JPA @Enumerated(STRING)). DB-agnostic.
 */
public enum ConsentMode {
    WRITTEN,
    DIGITAL,
    VERBAL
}
