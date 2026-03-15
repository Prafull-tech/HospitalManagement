package com.hospital.hms.appointment.entity;

/**
 * Appointment audit event types.
 */
public enum AppointmentAuditEventType {
    CREATED,
    RESCHEDULED,
    CANCELLED,
    NO_SHOW,
    CONVERTED_TO_OPD
}
