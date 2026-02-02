package com.hospital.hms.hospital.config;

/**
 * Role names for Bed Availability API (Spring Security).
 * Used in @PreAuthorize and authority checks. No hardcoding in controller.
 * <ul>
 *   <li>ADMIN, SUPER_ADMIN → POST, GET, PUT, DELETE</li>
 *   <li>IPD_MANAGER → GET, PUT</li>
 *   <li>DOCTOR, RECEPTIONIST, NURSE, HELP_DESK → GET only</li>
 * </ul>
 */
public final class BedAvailabilityRoles {

    public static final String ADMIN = "ADMIN";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String IPD_MANAGER = "IPD_MANAGER";
    public static final String DOCTOR = "DOCTOR";
    public static final String RECEPTIONIST = "RECEPTIONIST";
    public static final String NURSE = "NURSE";
    public static final String HELP_DESK = "HELP_DESK";

    /** Expression for create/delete: ADMIN, SUPER_ADMIN only */
    public static final String ADMIN_ONLY = "hasAnyRole('" + ADMIN + "','" + SUPER_ADMIN + "')";

    /** Expression for read: ADMIN, SUPER_ADMIN, IPD_MANAGER, DOCTOR, RECEPTIONIST, NURSE, HELP_DESK (matches sidebar) */
    public static final String CAN_READ = "hasAnyRole('" + ADMIN + "','" + SUPER_ADMIN + "','" + IPD_MANAGER + "','" + DOCTOR + "','" + RECEPTIONIST + "','" + NURSE + "','" + HELP_DESK + "')";

    /** Expression for update: ADMIN, SUPER_ADMIN, IPD_MANAGER */
    public static final String CAN_UPDATE = "hasAnyRole('" + ADMIN + "','" + SUPER_ADMIN + "','" + IPD_MANAGER + "')";

    private BedAvailabilityRoles() {
    }
}
