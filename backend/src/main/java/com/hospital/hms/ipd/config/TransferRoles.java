package com.hospital.hms.ipd.config;

/**
 * Role-based access for IPD transfer APIs.
 * DOCTOR → recommend; IPD_MANAGER → approve (consent, confirm-bed); NURSE → execute; ADMIN → full.
 */
public final class TransferRoles {

    public static final String ADMIN = "ADMIN";
    public static final String DOCTOR = "DOCTOR";
    public static final String IPD_MANAGER = "IPD_MANAGER";
    public static final String NURSE = "NURSE";

    /** Recommend transfer: DOCTOR or ADMIN */
    public static final String CAN_RECOMMEND = "hasAnyRole('" + DOCTOR + "','" + ADMIN + "')";

    /** Approve (consent, confirm-bed): IPD_MANAGER or ADMIN */
    public static final String CAN_APPROVE = "hasAnyRole('" + IPD_MANAGER + "','" + ADMIN + "')";

    /** Execute transfer: NURSE or ADMIN */
    public static final String CAN_EXECUTE = "hasAnyRole('" + NURSE + "','" + ADMIN + "')";

    /** Read transfers: DOCTOR, IPD_MANAGER, NURSE, ADMIN */
    public static final String CAN_READ = "hasAnyRole('" + DOCTOR + "','" + IPD_MANAGER + "','" + NURSE + "','" + ADMIN + "')";

    private TransferRoles() {
    }
}
