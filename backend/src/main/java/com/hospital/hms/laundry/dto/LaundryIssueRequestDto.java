package com.hospital.hms.laundry.dto;

import com.hospital.hms.laundry.entity.LinenType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for POST /api/laundry/issue.
 */
public class LaundryIssueRequestDto {

    @NotNull(message = "Ward name is required")
    @Size(max = 100)
    private String wardName;

    @NotNull(message = "Linen type is required")
    private LinenType linenType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private Long ipdAdmissionId;

    public LaundryIssueRequestDto() {
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public LinenType getLinenType() {
        return linenType;
    }

    public void setLinenType(LinenType linenType) {
        this.linenType = linenType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }
}
