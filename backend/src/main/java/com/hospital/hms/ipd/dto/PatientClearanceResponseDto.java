package com.hospital.hms.ipd.dto;

/**
 * Patient services clearance status for discharge.
 * Used by GET /api/patient/clearance/{ipdId}.
 * If any false → block discharge.
 */
public class PatientClearanceResponseDto {

    private boolean housekeeping;
    private boolean linen;
    private boolean dietary;

    public boolean isHousekeeping() {
        return housekeeping;
    }

    public void setHousekeeping(boolean housekeeping) {
        this.housekeeping = housekeeping;
    }

    public boolean isLinen() {
        return linen;
    }

    public void setLinen(boolean linen) {
        this.linen = linen;
    }

    public boolean isDietary() {
        return dietary;
    }

    public void setDietary(boolean dietary) {
        this.dietary = dietary;
    }
}
