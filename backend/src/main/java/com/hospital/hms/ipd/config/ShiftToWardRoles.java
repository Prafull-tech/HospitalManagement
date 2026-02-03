package com.hospital.hms.ipd.config;

/**
 * Roles allowed to perform shift-to-ward (nursing staff). Used in @PreAuthorize.
 */
public final class ShiftToWardRoles {

    /** SpEL for @PreAuthorize: only nursing roles (and ADMIN) may call shift-to-ward. */
    public static final String CAN_SHIFT_TO_WARD = "hasAnyRole('ADMIN','NURSE')";

    private ShiftToWardRoles() {
    }
}
