package com.hospital.hms.lab.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request for entering a single lab result (by orderItemId).
 * Fields: testValue, unit, referenceRange, remarks.
 */
public class LabResultEntryRequestDto {

    @NotNull(message = "Order item ID is required")
    @JsonAlias("orderItemId")
    private Long orderItemId;

    @NotNull(message = "Result value is required")
    @JsonAlias("testValue")
    @Size(max = 1000)
    private String resultValue;

    @Size(max = 100)
    private String unit;

    @Size(max = 1000)
    @JsonAlias("referenceRange")
    private String normalRange;

    @Size(max = 1000)
    private String remarks;

    public LabResultEntryRequestDto() {
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getNormalRange() {
        return normalRange;
    }

    public void setNormalRange(String normalRange) {
        this.normalRange = normalRange;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
