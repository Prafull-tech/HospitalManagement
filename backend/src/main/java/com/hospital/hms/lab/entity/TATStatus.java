package com.hospital.hms.lab.entity;

/**
 * Turnaround Time (TAT) compliance status for a test.
 */
public enum TATStatus {
    WITHIN_TAT,        // Test completed within defined TAT
    BREACH             // TAT breached, delay reason captured
}
