package com.hospital.hms.lab.entity;

/**
 * Priority level for laboratory tests (affects TAT and queue ordering).
 */
public enum PriorityLevel {
    ROUTINE,
    PRIORITY,          // Emergency / ICU tests marked PRIORITY
    STAT               // Stat tests (highest priority)
}
