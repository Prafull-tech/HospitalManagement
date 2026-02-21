package com.hospital.hms.pharmacy.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Rack inventory report: rack details + medicines stored.
 */
public class RackInventoryDto {

    private Long rackId;
    private String rackCode;
    private String rackName;
    private String locationArea;
    private String storageType;
    private List<RackInventoryItemDto> items = new ArrayList<>();

    public Long getRackId() {
        return rackId;
    }

    public void setRackId(Long rackId) {
        this.rackId = rackId;
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

    public String getLocationArea() {
        return locationArea;
    }

    public void setLocationArea(String locationArea) {
        this.locationArea = locationArea;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public List<RackInventoryItemDto> getItems() {
        return items;
    }

    public void setItems(List<RackInventoryItemDto> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
