package com.hospital.hms.ward.entity;

/**
 * Operational status of a room (not individual beds).
 * Used for housekeeping, maintenance, infection control.
 */
public enum RoomStatus {
    ACTIVE,
    CLEANING,
    MAINTENANCE,
    ISOLATION
}

