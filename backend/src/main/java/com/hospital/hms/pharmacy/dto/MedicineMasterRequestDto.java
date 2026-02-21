package com.hospital.hms.pharmacy.dto;

import com.hospital.hms.pharmacy.entity.MedicineCategory;
import com.hospital.hms.pharmacy.entity.MedicineForm;
import com.hospital.hms.pharmacy.entity.StorageType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MedicineMasterRequestDto {

    @NotBlank
    @Size(max = 50)
    private String medicineCode;

    @NotBlank
    @Size(max = 255)
    private String medicineName;

    @NotNull
    private MedicineCategory category;

    @Size(max = 50)
    private String strength;

    @NotNull
    private MedicineForm form;

    @NotNull
    @Min(0)
    private Integer minStock;

    @Min(0)
    private Integer quantity = 0;

    @NotNull
    private Boolean lasaFlag;

    @NotNull
    private StorageType storageType;

    private Boolean active = true;

    @Size(max = 255)
    private String manufacturer;

    @Size(max = 1000)
    private String notes;

    private Long rackId;
    private Long shelfId;
    @Size(max = 20)
    private String binNumber;
    @Size(max = 50)
    private String barcode;

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

    public MedicineCategory getCategory() {
        return category;
    }

    public void setCategory(MedicineCategory category) {
        this.category = category;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public MedicineForm getForm() {
        return form;
    }

    public void setForm(MedicineForm form) {
        this.form = form;
    }

    public Integer getMinStock() {
        return minStock;
    }

    public void setMinStock(Integer minStock) {
        this.minStock = minStock;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Boolean getLasaFlag() {
        return lasaFlag;
    }

    public void setLasaFlag(Boolean lasaFlag) {
        this.lasaFlag = lasaFlag;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getBinNumber() {
        return binNumber;
    }

    public void setBinNumber(String binNumber) {
        this.binNumber = binNumber;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}

