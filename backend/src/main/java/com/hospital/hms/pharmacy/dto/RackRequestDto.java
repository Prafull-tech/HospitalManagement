package com.hospital.hms.pharmacy.dto;

import com.hospital.hms.pharmacy.entity.LocationArea;
import com.hospital.hms.pharmacy.entity.RackCategoryType;
import com.hospital.hms.pharmacy.entity.StorageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RackRequestDto {

    @NotBlank
    @Size(max = 20)
    private String rackCode;

    @NotBlank
    @Size(max = 100)
    private String rackName;

    @NotNull
    private LocationArea locationArea;

    @NotNull
    private StorageType storageType;

    private RackCategoryType categoryType;

    private Boolean lasaSafe = false;

    private Integer maxCapacity = 100;

    private Boolean active = true;

    public String getRackCode() {
        return rackCode;
    }

    public void setRackCode(String rackCode) {
        this.rackCode = rackCode;
    }

    public String getRackName() {
        return rackName;
    }

    public void setRackName(String rackName) {
        this.rackName = rackName;
    }

    public LocationArea getLocationArea() {
        return locationArea;
    }

    public void setLocationArea(LocationArea locationArea) {
        this.locationArea = locationArea;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public RackCategoryType getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(RackCategoryType categoryType) {
        this.categoryType = categoryType;
    }

    public Boolean getLasaSafe() {
        return lasaSafe;
    }

    public void setLasaSafe(Boolean lasaSafe) {
        this.lasaSafe = lasaSafe;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
