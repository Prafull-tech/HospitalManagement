package com.hospital.hms.token.entity;

/**
 * Token priority for queue ordering. EMERGENCY and SENIOR get precedence.
 */
public enum TokenPriority {
    NORMAL,
    EMERGENCY,
    SENIOR,
    FOLLOWUP,
    PREGNANT
}
