package com.hospital.hms.doctor.entity;

/**
 * Doctor status: ACTIVE, INACTIVE, ON_LEAVE.
 * DB-agnostic: stored as string.
 */
public enum DoctorStatus {
    ACTIVE,
    INACTIVE,
    ON_LEAVE
}
