package com.hospital.hms.ipd.entity;

/**
 * Type of patient transfer for Transfer Policy Master.
 * INTERNAL: within same hospital (e.g. ward to ward).
 * EXTERNAL: to/from another facility; flagged separately for safe and legal handling.
 * Stored as string in DB (JPA @Enumerated(STRING)). DB-agnostic.
 */
public enum TransferType {
    INTERNAL,
    EXTERNAL
}
