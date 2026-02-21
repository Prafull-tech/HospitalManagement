package com.hospital.hms.pharmacy.dto;

import com.hospital.hms.pharmacy.entity.LocationArea;
import com.hospital.hms.pharmacy.entity.RackCategoryType;
import com.hospital.hms.pharmacy.entity.StorageType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RackResponseDto {

    private Long id;
    private String rackCode;
    private String rackName;
    private LocationArea locationArea;
    private StorageType storageType;
    private RackCategoryType categoryType;
    private Boolean lasaSafe;
    private Integer maxCapacity;
    private Boolean active;
    private String createdByUser;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ShelfResponseDto> shelves = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(String createdByUser) {
        this.createdByUser = createdByUser;
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

    public List<ShelfResponseDto> getShelves() {
        return shelves;
    }

    public void setShelves(List<ShelfResponseDto> shelves) {
        this.shelves = shelves != null ? shelves : new ArrayList<>();
    }
}
