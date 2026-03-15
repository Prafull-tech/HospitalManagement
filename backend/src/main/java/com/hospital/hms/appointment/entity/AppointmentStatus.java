package com.hospital.hms.appointment.entity;

/**
 * Appointment status. DB-agnostic: stored as string.
 */
public enum AppointmentStatus {
    BOOKED,
    CONFIRMED,
    PENDING_CONFIRMATION,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
