package com.hospital.hms.ipd.entity;

/**
 * Action type for transfer audit log. DB-agnostic (stored as string).
 */
public enum TransferAuditAction {
    RECOMMENDED,
    CONSENT_RECORDED,
    BED_CONFIRMED,
    EXECUTED
}
