package com.hospital.hms.laundry.dto;

import com.hospital.hms.laundry.entity.LaundryStatus;
import com.hospital.hms.laundry.entity.LinenType;

import java.time.Instant;

/**
 * Response DTO for linen inventory (GET /api/laundry/status).
 */
public class LinenInventoryResponseDto {

    private Long id;
    private LinenType linenType;
    private String wardName;
    private int quantityIssued;
    private int quantityReturned;
    private LaundryStatus laundryStatus;
    private Long ipdAdmissionId;
    private Instant createdAt;
    private Instant updatedAt;

    public LinenInventoryResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LinenType getLinenType() {
        return linenType;
    }

    public void setLinenType(LinenType linenType) {
        this.linenType = linenType;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public int getQuantityIssued() {
        return quantityIssued;
    }

    public void setQuantityIssued(int quantityIssued) {
        this.quantityIssued = quantityIssued;
    }

    public int getQuantityReturned() {
        return quantityReturned;
    }

    public void setQuantityReturned(int quantityReturned) {
        this.quantityReturned = quantityReturned;
    }

    public LaundryStatus getLaundryStatus() {
        return laundryStatus;
    }

    public void setLaundryStatus(LaundryStatus laundryStatus) {
        this.laundryStatus = laundryStatus;
    }

    public Long getIpdAdmissionId() {
        return ipdAdmissionId;
    }

    public void setIpdAdmissionId(Long ipdAdmissionId) {
        this.ipdAdmissionId = ipdAdmissionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
