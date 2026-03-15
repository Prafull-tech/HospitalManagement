package com.hospital.hms.appointment.dto;

import jakarta.validation.constraints.Size;

public class CancelRequestDto {

    @Size(max = 500)
    private String reason;

    public CancelRequestDto() {
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
