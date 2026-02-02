package com.hospital.hms.ipd.entity;

/**
 * Source of IPD admission. DB-agnostic: stored as string.
 */
public enum AdmissionType {
    OPD_REFERRAL,
    EMERGENCY,
    DIRECT
}
