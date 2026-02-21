package com.hospital.hms.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request for barcode/GTIN entry.
 * When medicine exists: add batch only (medicineId populated from lookup).
 * When medicine not found: create new (medicine fields required).
 */
public class BarcodeEntryRequestDto {

    @NotBlank
    @Size(max = 50)
    private String barcode;

    @Size(max = 50)
    private String batchNumber;

    private LocalDate expiryDate;

    @NotNull
    @Min(0)
    private Integer quantity = 0;

    private Long rackId;
    private Long shelfId;

    /** When barcode not found: create new medicine. Otherwise ignored. */
    @Valid
    private MedicineMasterRequestDto createNewMedicine;

    /** When saving from external lookup, set true for audit. */
    private Boolean fromExternalLookup;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
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

    public Long getRackId() {
        return rackId;
    }

    public void setRackId(Long rackId) {
        this.rackId = rackId;
    }

    public Long getShelfId() {
        return shelfId;
    }

    public void setShelfId(Long shelfId) {
        this.shelfId = shelfId;
    }

    public MedicineMasterRequestDto getCreateNewMedicine() {
        return createNewMedicine;
    }

    public void setCreateNewMedicine(MedicineMasterRequestDto createNewMedicine) {
        this.createNewMedicine = createNewMedicine;
    }

    public Boolean getFromExternalLookup() {
        return fromExternalLookup;
    }

    public void setFromExternalLookup(Boolean fromExternalLookup) {
        this.fromExternalLookup = fromExternalLookup;
    }
}
