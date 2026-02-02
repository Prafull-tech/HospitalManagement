package com.hospital.hms.ipd.entity;

/**
 * Workflow status for patient transfer execution.
 * RECOMMENDED → CONSENTED → BED_RESERVED → IN_TRANSIT → COMPLETED.
 * CANCELLED can occur from any prior state.
 * Stored as string in DB (JPA @Enumerated(STRING)). DB-agnostic.
 */
public enum TransferStatus {
    RECOMMENDED,
    CONSENTED,
    BED_RESERVED,
    IN_TRANSIT,
    COMPLETED,
    CANCELLED
}
