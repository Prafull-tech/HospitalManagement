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
 * Medicine master (configuration). Managed by pharmacy manager / store-in-charge only.
 * No hard delete; deactivate via active=false for audit safety.
 */
@Entity
@Table(
        name = "medicine_master",
        indexes = {
                @Index(name = "idx_medicine_code", columnList = "medicine_code", unique = true),
                @Index(name = "idx_medicine_active", columnList = "active")
        }
)
public class MedicineMaster extends BaseIdEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "medicine_code", nullable = false, unique = true, length = 50)
    private String medicineCode;

    @NotBlank
    @Size(max = 255)
    @Column(name = "medicine_name", nullable = false, length = 255)
    private String medicineName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private MedicineCategory category;

    @Size(max = 50)
    @Column(name = "strength", length = 50)
    private String strength;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "form", nullable = false, length = 20)
    private MedicineForm form;

    @NotNull
    @Column(name = "min_stock", nullable = false)
    private Integer minStock;

    @NotNull
    @Column(name = "lasa_flag", nullable = false)
    private Boolean lasaFlag = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false, length = 20)
    private StorageType storageType;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Size(max = 255)
    @Column(name = "manufacturer", length = 255)
    private String manufacturer;

    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    private String notes;

    @Size(max = 255)
    @Column(name = "created_by_user", length = 255)
    private String createdByUser;

    public MedicineMaster() {
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

    public String getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(String createdByUser) {
        this.createdByUser = createdByUser;
    }
}

