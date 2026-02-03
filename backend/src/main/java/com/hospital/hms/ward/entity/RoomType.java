package com.hospital.hms.ward.entity;

/**
 * Type of room within a ward.
 * Stored as STRING in DB (JPA @Enumerated). DB-agnostic (H2 & MySQL).
 */
public enum RoomType {
    SHARED,
    PRIVATE
}

