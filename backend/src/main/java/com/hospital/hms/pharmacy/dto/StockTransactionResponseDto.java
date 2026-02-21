package com.hospital.hms.pharmacy.dto;

import com.hospital.hms.pharmacy.entity.SaleType;
import com.hospital.hms.pharmacy.entity.StockTransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class StockTransactionResponseDto {

    private Long id;
    private Long medicineId;
    private String medicineCode;
    private String medicineName;
    private StockTransactionType transactionType;
    private int quantity;
    private LocalDate transactionDate;
    private String batchNumber;
    private LocalDate expiryDate;
    private String supplier;
    private String reference;
    private SaleType saleType;
    private Long patientId;
    private String manualPatientName;
    private String manualPhone;
    private String manualEmail;
    private String manualAddress;
    private BigDecimal costPerUnit;
    private String notes;
    private String performedBy;
    private Instant performedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public StockTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(StockTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public SaleType getSaleType() {
        return saleType;
    }

    public void setSaleType(SaleType saleType) {
        this.saleType = saleType;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getManualPatientName() {
        return manualPatientName;
    }

    public void setManualPatientName(String manualPatientName) {
        this.manualPatientName = manualPatientName;
    }

    public String getManualPhone() {
        return manualPhone;
    }

    public void setManualPhone(String manualPhone) {
        this.manualPhone = manualPhone;
    }

    public String getManualEmail() {
        return manualEmail;
    }

    public void setManualEmail(String manualEmail) {
        this.manualEmail = manualEmail;
    }

    public String getManualAddress() {
        return manualAddress;
    }

    public void setManualAddress(String manualAddress) {
        this.manualAddress = manualAddress;
    }

    public BigDecimal getCostPerUnit() {
        return costPerUnit;
    }

    public void setCostPerUnit(BigDecimal costPerUnit) {
        this.costPerUnit = costPerUnit;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public Instant getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(Instant performedAt) {
        this.performedAt = performedAt;
    }
}
