package com.hospital.hms.pharmacy.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Rack master for pharmacy storage organization.
 * No hard delete; deactivate via active=false for audit safety.
 */
@Entity
@Table(
        name = "pharmacy_rack",
        indexes = {
                @Index(name = "idx_rack_code", columnList = "rack_code", unique = true),
                @Index(name = "idx_rack_active", columnList = "active")
        }
)
public class PharmacyRack extends BaseIdEntity {

    @NotBlank
    @Size(max = 20)
    @Column(name = "rack_code", nullable = false, unique = true, length = 20)
    private String rackCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "rack_name", nullable = false, length = 100)
    private String rackName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "location_area", nullable = false, length = 30)
    private LocationArea locationArea;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false, length = 20)
    private StorageType storageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", length = 30)
    private RackCategoryType categoryType;

    @Column(name = "lasa_safe")
    private Boolean lasaSafe = false;

    @Column(name = "max_capacity")
    private Integer maxCapacity = 100;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Size(max = 255)
    @Column(name = "created_by_user", length = 255)
    private String createdByUser;

    public PharmacyRack() {
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
}
