package com.hospital.hms.ipd.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for discharge (initiate or finalize). Optional remarks.
 */
public class IPDDischargeRequestDto {

    @Size(max = 500)
    private String dischargeRemarks;

    public IPDDischargeRequestDto() {
    }

    public String getDischargeRemarks() {
        return dischargeRemarks;
    }

    public void setDischargeRemarks(String dischargeRemarks) {
        this.dischargeRemarks = dischargeRemarks;
    }
}
