package com.hospital.hms.ipd.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for transferring an admission to another bed.
 */
public class IPDTransferRequestDto {

    @NotNull(message = "Target bed ID is required")
    private Long bedId;

    @Size(max = 500)
    private String remarks;

    public IPDTransferRequestDto() {
    }

    public Long getBedId() {
        return bedId;
    }

    public void setBedId(Long bedId) {
        this.bedId = bedId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
