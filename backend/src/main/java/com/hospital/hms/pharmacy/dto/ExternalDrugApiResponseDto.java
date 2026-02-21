package com.hospital.hms.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for mapping 3rd party drug API response.
 * Field names may vary by provider; use @JsonProperty for flexibility.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalDrugApiResponseDto {

    @JsonProperty("medicineName")
    private String medicineName;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("strength")
    private String strength;

    @JsonProperty("form")
    private String form;

    @JsonProperty("manufacturer")
    private String manufacturer;

    @JsonProperty("category")
    private String category;

    @JsonProperty("activeIngredient")
    private String activeIngredient;

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getActiveIngredient() {
        return activeIngredient;
    }

    public void setActiveIngredient(String activeIngredient) {
        this.activeIngredient = activeIngredient;
    }

    /** Resolve display name from any of the common fields. */
    public String resolveMedicineName() {
        if (medicineName != null && !medicineName.isBlank()) return medicineName.trim();
        if (productName != null && !productName.isBlank()) return productName.trim();
        if (name != null && !name.isBlank()) return name.trim();
        if (activeIngredient != null && !activeIngredient.isBlank()) return activeIngredient.trim();
        return null;
    }
}
