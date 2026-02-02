package com.hospital.hms.hospital.service;

/**
 * Caller role for bed availability operations (logical access, not security).
 * ADMIN: full access (create, read, update, delete).
 * IPD_MANAGER: update and read only.
 * DOCTOR: read only.
 */
public enum BedAvailabilityCallerRole {
    ADMIN,
    IPD_MANAGER,
    DOCTOR
}
