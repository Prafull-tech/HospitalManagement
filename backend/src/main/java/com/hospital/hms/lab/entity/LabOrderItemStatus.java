package com.hospital.hms.lab.entity;

/**
 * Status of a lab order item (line).
 */
public enum LabOrderItemStatus {
    ORDERED,
    COLLECTED,
    IN_PROGRESS,
    COMPLETED,
    VERIFIED,
    RELEASED,
    REJECTED,
    CANCELLED
}
