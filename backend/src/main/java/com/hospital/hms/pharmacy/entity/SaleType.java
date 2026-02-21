package com.hospital.hms.pharmacy.entity;

/**
 * Sale type: linked to registered patient (OPD/IPD) or manual walk-in.
 */
public enum SaleType {
    PATIENT,  // Linked to patient from system
    MANUAL    // Walk-in, manual entry
}
