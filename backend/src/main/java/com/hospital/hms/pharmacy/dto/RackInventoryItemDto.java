package com.hospital.hms.pharmacy.dto;

import com.hospital.hms.pharmacy.entity.StorageType;

/**
 * Item in rack inventory: medicine at a shelf/bin location.
 */
public class RackInventoryItemDto {

    private Long medicineId;
    private String medicineCode;
    private String medicineName;
    private StorageType storageType;
    private boolean lasa;
    private String shelfCode;
    private Integer shelfLevel;
    private String binNumber;
    private int batchCount;
    private String nearestExpiry;
    private String expiryRiskClass; // text-success, text-warning, text-danger

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineCode() {
        return medicineCode;
    }

    public void setMedicineCode(String medicineCode) {
        this.medicineCode = medicineCode;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public boolean isLasa() {
        return lasa;
    }

    public void setLasa(boolean lasa) {
        this.lasa = lasa;
    }

    public String getShelfCode() {
        return shelfCode;
    }

    public void setShelfCode(String shelfCode) {
        this.shelfCode = shelfCode;
    }

    public Integer getShelfLevel() {
        return shelfLevel;
    }

    public void setShelfLevel(Integer shelfLevel) {
        this.shelfLevel = shelfLevel;
    }

    public String getBinNumber() {
        return binNumber;
    }

    public void setBinNumber(String binNumber) {
        this.binNumber = binNumber;
    }

    public int getBatchCount() {
        return batchCount;
    }

    public void setBatchCount(int batchCount) {
        this.batchCount = batchCount;
    }

    public String getNearestExpiry() {
        return nearestExpiry;
    }

    public void setNearestExpiry(String nearestExpiry) {
        this.nearestExpiry = nearestExpiry;
    }

    public String getExpiryRiskClass() {
        return expiryRiskClass;
    }

    public void setExpiryRiskClass(String expiryRiskClass) {
        this.expiryRiskClass = expiryRiskClass;
    }
}
