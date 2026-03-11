package com.hospital.hms.lab.config;

/**
 * Role-based access for Lab module (Section 13).
 * <ul>
 *   <li>Lab Technician: Sample collection, Sample processing, Result entry</li>
 *   <li>Senior Technician / Pathologist: Result verification</li>
 *   <li>Doctor: View reports</li>
 *   <li>Admin: Manage test master</li>
 * </ul>
 */
public final class LabRoleAccess {

    public static final String ADMIN = "ADMIN";
    public static final String LAB_TECHNICIAN = "LAB_TECHNICIAN";
    public static final String LAB_SUPERVISOR = "LAB_SUPERVISOR";
    public static final String PATHOLOGIST = "PATHOLOGIST";
    public static final String PHLEBOTOMIST = "PHLEBOTOMIST";
    public static final String DOCTOR = "DOCTOR";

    /** Lab Technician: sample collection, processing, result entry */
    public static final String LAB_TECHNICIAN_ROLES =
            "hasAnyRole('" + LAB_TECHNICIAN + "','" + PHLEBOTOMIST + "','" + ADMIN + "')";

    /** Sample reject: Lab Technician or Senior (collection/quality rejection) */
    public static final String SAMPLE_REJECT_ROLES =
            "hasAnyRole('" + LAB_TECHNICIAN + "','" + PHLEBOTOMIST + "','" + LAB_SUPERVISOR + "','" + PATHOLOGIST + "','" + ADMIN + "')";

    /** Senior Technician / Pathologist / Lab Technician: result verification */
    public static final String RESULT_VERIFICATION_ROLES =
            "hasAnyRole('" + LAB_TECHNICIAN + "','" + LAB_SUPERVISOR + "','" + PATHOLOGIST + "','" + ADMIN + "')";

    /** Doctor: view reports. Lab roles included for workflow (generate, verify, view). */
    public static final String VIEW_REPORTS_ROLES =
            "hasAnyRole('" + DOCTOR + "','" + LAB_TECHNICIAN + "','" + LAB_SUPERVISOR + "','" + PATHOLOGIST + "','" + ADMIN + "')";

    /** View orders, items, results (Lab workflow + Doctor) */
    public static final String VIEW_ORDERS_AND_RESULTS_ROLES =
            "hasAnyRole('" + LAB_TECHNICIAN + "','" + LAB_SUPERVISOR + "','" + PATHOLOGIST + "','" + PHLEBOTOMIST + "','" + DOCTOR + "','" + ADMIN + "')";

    /** TAT breaches: Lab, Doctor, Quality Manager */
    public static final String TAT_BREACHES_ROLES =
            "hasAnyRole('" + LAB_SUPERVISOR + "','" + PATHOLOGIST + "','" + DOCTOR + "','QUALITY_MANAGER','" + ADMIN + "')";

    /** Admin: manage test master */
    public static final String MANAGE_TEST_MASTER_ROLES =
            "hasRole('" + ADMIN + "')";

    private LabRoleAccess() {
    }
}
