package com.hospital.hms.system.entity;

/**
 * Action-level permission. DB-agnostic enum (stored as string in JPA).
 */
public enum ActionType {
    VIEW,
    CREATE,
    UPDATE,
    DELETE,
    APPROVE
}
