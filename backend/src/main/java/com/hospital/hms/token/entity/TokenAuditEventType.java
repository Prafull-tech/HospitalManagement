package com.hospital.hms.token.entity;

/**
 * Token audit event types for tracking lifecycle.
 */
public enum TokenAuditEventType {
    GENERATED,
    CALLED,
    STARTED_CONSULTATION,
    COMPLETED,
    SKIPPED
}
