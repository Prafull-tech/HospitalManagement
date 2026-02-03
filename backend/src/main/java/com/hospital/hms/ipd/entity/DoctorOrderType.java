package com.hospital.hms.ipd.entity;

/**
 * Type of doctor order. Linked with IPD Admission; when executed, charge can be posted to billing.
 */
public enum DoctorOrderType {
    MEDICATION,
    LAB,
    PROCEDURE,
    CONSULTATION,
    DIET,
    OTHER
}
