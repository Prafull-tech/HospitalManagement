package com.hospital.hms.ipd.entity;

/**
 * Equipment used during patient transfer.
 * Stored as string in DB (JPA @Enumerated(STRING)). DB-agnostic.
 */
public enum EquipmentType {
    OXYGEN,
    MONITOR
}
