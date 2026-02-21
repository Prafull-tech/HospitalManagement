package com.hospital.hms.pharmacy.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Shelf within a rack. Supports bin-level organization.
 */
@Entity
@Table(
        name = "pharmacy_shelf",
        indexes = {
                @Index(name = "idx_shelf_rack", columnList = "rack_id"),
                @Index(name = "idx_shelf_code_rack", columnList = "rack_id, shelf_code", unique = true)
        }
)
public class PharmacyShelf extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rack_id", nullable = false)
    private PharmacyRack rack;

    @NotBlank
    @Size(max = 30)
    @Column(name = "shelf_code", nullable = false, length = 30)
    private String shelfCode;

    @NotNull
    @Column(name = "shelf_level", nullable = false)
    private Integer shelfLevel;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Size(max = 20)
    @Column(name = "bin_number", length = 20)
    private String binNumber;

    public PharmacyShelf() {
    }

    public PharmacyRack getRack() {
        return rack;
    }

    public void setRack(PharmacyRack rack) {
        this.rack = rack;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getBinNumber() {
        return binNumber;
    }

    public void setBinNumber(String binNumber) {
        this.binNumber = binNumber;
    }
}
