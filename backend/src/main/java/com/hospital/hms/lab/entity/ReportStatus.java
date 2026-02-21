package com.hospital.hms.lab.entity;

/**
 * Status of a laboratory report throughout its lifecycle.
 */
public enum ReportStatus {
    DRAFT,          // Report generated, not yet verified
    VERIFIED,       // Verified by supervisor, awaiting release
    RELEASED        // Released to doctor/patient, read-only
}
