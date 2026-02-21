package com.hospital.hms.pharmacy.entity;

/**
 * Ward/source type for medication orders. Maps to IPD ward, OPD, or Emergency.
 */
public enum MedicationOrderWardType {
    ICU,
    GENERAL,
    EMERGENCY,
    OPD
}
