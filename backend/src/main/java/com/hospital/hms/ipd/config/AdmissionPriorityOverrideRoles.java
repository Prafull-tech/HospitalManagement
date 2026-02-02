package com.hospital.hms.ipd.config;

import java.util.Set;

/**
 * Authority roles allowed to override admission priority.
 * Used in @PreAuthorize and service-level checks. No hardcoding elsewhere.
 * <ul>
 *   <li>MEDICAL_SUPERINTENDENT</li>
 *   <li>EMERGENCY_HEAD</li>
 *   <li>IPD_MANAGER</li>
 * </ul>
 * Override requires a reason and is logged for audit.
 */
public final class AdmissionPriorityOverrideRoles {

    public static final String MEDICAL_SUPERINTENDENT = "MEDICAL_SUPERINTENDENT";
    public static final String EMERGENCY_HEAD = "EMERGENCY_HEAD";
    public static final String IPD_MANAGER = "IPD_MANAGER";

    /** Roles that may override admission priority. */
    public static final Set<String> OVERRIDE_AUTHORITY_ROLES = Set.of(
            MEDICAL_SUPERINTENDENT,
            EMERGENCY_HEAD,
            IPD_MANAGER
    );

    /** SpEL expression for @PreAuthorize: only these roles can call override endpoint. */
    public static final String CAN_OVERRIDE_PRIORITY =
            "hasAnyRole('" + MEDICAL_SUPERINTENDENT + "','" + EMERGENCY_HEAD + "','" + IPD_MANAGER + "')";

    /** SpEL expression for @PreAuthorize: roles that may read priority master and call evaluate. */
    public static final String CAN_READ_PRIORITY =
            "hasAnyRole('ADMIN','" + IPD_MANAGER + "','DOCTOR','" + MEDICAL_SUPERINTENDENT + "','" + EMERGENCY_HEAD + "')";

    private AdmissionPriorityOverrideRoles() {
    }
}
