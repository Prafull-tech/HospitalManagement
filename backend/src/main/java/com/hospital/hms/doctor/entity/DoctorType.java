package com.hospital.hms.doctor.entity;

/**
 * Type of doctor: Consultant, RMO, Resident, Duty Doctor.
 * DB-agnostic: stored as string (e.g. CONSULTANT).
 */
public enum DoctorType {
    CONSULTANT,
    RMO,
    RESIDENT,
    DUTY_DOCTOR
}
