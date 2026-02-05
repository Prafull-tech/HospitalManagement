package com.hospital.hms.pharmacy.dto;

public class IpdIssueQueueLineDto {

    private String medicineCode;
    private String medicineName;
    private int requestedQty;
    private int availableQty;
    private String nextBatchNumber;
    private String nextBatchExpiryDisplay;
    private String expiryRiskClass;
    private boolean lasa;

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

    public int getRequestedQty() {
        return requestedQty;
    }

    public void setRequestedQty(int requestedQty) {
        this.requestedQty = requestedQty;
    }

    public int getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(int availableQty) {
        this.availableQty = availableQty;
    }

    public String getNextBatchNumber() {
        return nextBatchNumber;
    }

    public void setNextBatchNumber(String nextBatchNumber) {
        this.nextBatchNumber = nextBatchNumber;
    }

    public String getNextBatchExpiryDisplay() {
        return nextBatchExpiryDisplay;
    }

    public void setNextBatchExpiryDisplay(String nextBatchExpiryDisplay) {
        this.nextBatchExpiryDisplay = nextBatchExpiryDisplay;
    }

    public String getExpiryRiskClass() {
        return expiryRiskClass;
    }

    public void setExpiryRiskClass(String expiryRiskClass) {
        this.expiryRiskClass = expiryRiskClass;
    }

    public boolean isLasa() {
        return lasa;
    }

    public void setLasa(boolean lasa) {
        this.lasa = lasa;
    }
}

