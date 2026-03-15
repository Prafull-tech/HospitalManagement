package com.hospital.hms.appointment.entity;

/**
 * How the appointment was created. DB-agnostic: stored as string.
 */
public enum AppointmentSource {
    FRONT_DESK,
    WALK_IN,
    ONLINE
}
