package com.hospital.hms.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Create/update hospital. hospitalCode must be unique across non-deleted hospitals.
 */
public class HospitalRequestDto {

    @NotBlank(message = "Hospital code is required")
    @Size(max = 50, message = "Hospital code must not exceed 50 characters")
    private String hospitalCode;

    @NotBlank(message = "Hospital name is required")
    @Size(max = 255, message = "Hospital name must not exceed 255 characters")
    private String hospitalName;

    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;

    private Boolean active = true;

    public String getHospitalCode() {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode) {
        this.hospitalCode = hospitalCode;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
