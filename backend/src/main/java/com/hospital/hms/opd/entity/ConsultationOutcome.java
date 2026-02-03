package com.hospital.hms.opd.entity;

/**
 * Doctor consultation result. Must be one of these for OPD/Emergency visit outcome.
 * DB-agnostic: stored as string.
 */
public enum ConsultationOutcome {
    /** OPD treatment only; no lab or admission. */
    OPD_TREATMENT_ONLY,
    /** Lab test advised. */
    LAB_TEST_ADVISED,
    /** IPD admission advised; doctor must explicitly mark "Admission Recommended" via API. */
    IPD_ADMISSION_ADVISED
}
