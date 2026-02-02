package com.hospital.hms.opd.entity;

/**
 * OPD visit status. DB-agnostic: stored as string.
 */
public enum VisitStatus {
    REGISTERED,
    IN_CONSULTATION,
    COMPLETED,
    REFERRED,
    CANCELLED
}
