package com.hospital.hms.ipd.entity;

/**
 * Admission priority code. P1 = highest priority.
 * Stored as string in DB (JPA @Enumerated(STRING)). DB-agnostic.
 */
public enum PriorityCode {
    P1,
    P2,
    P3,
    P4
}
