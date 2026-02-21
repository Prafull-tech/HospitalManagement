package com.hospital.hms.pharmacy.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.reception.entity.Patient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Stock movement: purchase (stock in) or sell (stock out).
 * NABH audit-ready.
 */
@Entity
@Table(
        name = "stock_transaction",
        indexes = {
                @Index(name = "idx_stock_txn_medicine", columnList = "medicine_id"),
                @Index(name = "idx_stock_txn_type_date", columnList = "transaction_type, transaction_date"),
                @Index(name = "idx_stock_txn_patient", columnList = "patient_id")
        }
)
public class StockTransaction extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private MedicineMaster medicine;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private StockTransactionType transactionType;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "supplier", length = 255)
    private String supplier;

    @Column(name = "reference", length = 100)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", length = 20)
    private SaleType saleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "manual_patient_name", length = 255)
    private String manualPatientName;

    @Column(name = "manual_phone", length = 20)
    private String manualPhone;

    @Column(name = "manual_email", length = 100)
    private String manualEmail;

    @Column(name = "manual_address", length = 500)
    private String manualAddress;

    @Column(name = "cost_per_unit", precision = 12, scale = 2)
    private java.math.BigDecimal costPerUnit;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "performed_by", nullable = false, length = 255)
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    public MedicineMaster getMedicine() {
        return medicine;
    }

    public void setMedicine(MedicineMaster medicine) {
        this.medicine = medicine;
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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
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

    public java.math.BigDecimal getCostPerUnit() {
        return costPerUnit;
    }

    public void setCostPerUnit(java.math.BigDecimal costPerUnit) {
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
