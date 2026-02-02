package com.hospital.hms.ipd.entity;

/**
 * Status of a transfer bed reservation.
 * RESERVED: bed held for transfer (must be AVAILABLE before reserve).
 * CONFIRMED: transfer completed, bed allocated to admission.
 * RELEASED: reservation cancelled/released before transfer.
 * CANCELLED: reservation explicitly cancelled.
 * Stored as string in DB (JPA @Enumerated(STRING)). DB-agnostic.
 */
public enum ReservationStatus {
    RESERVED,
    CONFIRMED,
    RELEASED,
    CANCELLED
}
