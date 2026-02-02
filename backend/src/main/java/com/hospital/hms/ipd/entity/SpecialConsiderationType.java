package com.hospital.hms.ipd.entity;

/**
 * Special admission consideration category. One config per type.
 * Stored as string in DB (JPA @Enumerated(STRING)). DB-agnostic.
 * <ul>
 *   <li>SENIOR_CITIZEN</li>
 *   <li>PREGNANT_WOMAN</li>
 *   <li>CHILD</li>
 *   <li>DISABLED_PATIENT</li>
 * </ul>
 */
public enum SpecialConsiderationType {
    SENIOR_CITIZEN,
    PREGNANT_WOMAN,
    CHILD,
    DISABLED_PATIENT
}
