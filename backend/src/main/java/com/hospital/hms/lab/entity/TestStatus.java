package com.hospital.hms.lab.entity;

/**
 * Status of a laboratory test order throughout its lifecycle.
 */
public enum TestStatus {
    ORDERED,           // Test ordered, awaiting sample collection
    COLLECTED,         // Sample collected, awaiting processing
    IN_PROGRESS,       // Test being performed
    COMPLETED,         // Test completed, awaiting verification
    VERIFIED,          // Results verified by supervisor
    RELEASED,          // Report released to doctor/patient
    REJECTED,          // Sample rejected (hemolysed, insufficient, etc.)
    CANCELLED          // Test cancelled with reason
}
