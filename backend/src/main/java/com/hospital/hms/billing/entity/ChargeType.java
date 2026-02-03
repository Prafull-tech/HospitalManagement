package com.hospital.hms.billing.entity;

/**
 * Type of charge for IPD admission. Used when modules auto-add charges to billing.
 */
public enum ChargeType {
    PHARMACY,
    LAB,
    DOCTOR_ORDER,
    CONSULTATION,
    NURSING,
    PROCEDURE,
    OTHER
}
