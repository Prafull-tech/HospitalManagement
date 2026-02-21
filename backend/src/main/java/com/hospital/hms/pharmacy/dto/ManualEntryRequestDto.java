package com.hospital.hms.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request for manual medicine entry (full form + batch).
 */
public class ManualEntryRequestDto {

    @Valid
    @NotNull
    private MedicineMasterRequestDto medicine;

    @Size(max = 50)
    private String batchNumber;

    private LocalDate expiryDate;

    @Min(0)
    private Integer quantity = 0;

    /** When saving after external lookup, pass barcode for audit. */
    @Size(max = 50)
    private String externalLookupBarcode;

    public MedicineMasterRequestDto getMedicine() {
        return medicine;
    }

    public void setMedicine(MedicineMasterRequestDto medicine) {
        this.medicine = medicine;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getExternalLookupBarcode() {
        return externalLookupBarcode;
    }

    public void setExternalLookupBarcode(String externalLookupBarcode) {
        this.externalLookupBarcode = externalLookupBarcode;
    }
}
