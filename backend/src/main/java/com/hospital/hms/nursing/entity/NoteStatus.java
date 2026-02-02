package com.hospital.hms.nursing.entity;

/**
 * Nursing note status for audit and locking. DB-agnostic: stored as string.
 */
public enum NoteStatus {
    DRAFT,
    LOCKED
}
