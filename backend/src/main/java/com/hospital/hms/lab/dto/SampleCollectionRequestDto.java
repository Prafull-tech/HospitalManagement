package com.hospital.hms.lab.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for sample collection.
 */
public class SampleCollectionRequestDto {

    @NotNull(message = "Test order ID is required")
    private Long testOrderId;

    @Size(max = 255)
    private String wardName; // For IPD

    @Size(max = 50)
    private String bedNumber; // For IPD

    @Size(max = 500)
    private String remarks;

    public SampleCollectionRequestDto() {
    }

    public Long getTestOrderId() {
        return testOrderId;
    }

    public void setTestOrderId(Long testOrderId) {
        this.testOrderId = testOrderId;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
