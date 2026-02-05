package com.hospital.hms.pharmacy.dto;

public class FefoStockRowDto {

    private String medicineCode;
    private String medicineName;
    private String batchNumber;
    private String expiryDate;
    private int quantityAvailable;
    private int fefoRank;
    private String riskLevel;
    private String riskColorClass;
    private boolean lasa;
    private String storageLocation;

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

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public int getFefoRank() {
        return fefoRank;
    }

    public void setFefoRank(int fefoRank) {
        this.fefoRank = fefoRank;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskColorClass() {
        return riskColorClass;
    }

    public void setRiskColorClass(String riskColorClass) {
        this.riskColorClass = riskColorClass;
    }

    public boolean isLasa() {
        return lasa;
    }

    public void setLasa(boolean lasa) {
        this.lasa = lasa;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }
}

