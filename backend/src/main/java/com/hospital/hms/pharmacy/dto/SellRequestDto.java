package com.hospital.hms.pharmacy.dto;

import com.hospital.hms.pharmacy.entity.SaleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SellRequestDto {

    /** Single-item mode (backward compat). Used when lineItems is null or empty. */
    private Long medicineId;

    @Min(1)
    private Integer quantity;

    @NotNull
    private LocalDate transactionDate;

    /** Multi-item mode. When present and non-empty, overrides medicineId/quantity. */
    private List<SellLineItemDto> lineItems;

    private SaleType saleType = SaleType.PATIENT;

    private Long patientId;

    @Size(max = 255)
    private String manualPatientName;

    @Size(max = 20)
    private String manualPhone;

    @Size(max = 100)
    private String manualEmail;

    @Size(max = 500)
    private String manualAddress;

    @Size(max = 100)
    private String reference;

    @Size(max = 500)
    private String notes;

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<SellLineItemDto> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<SellLineItemDto> lineItems) {
        this.lineItems = lineItems;
    }

    /** Resolve effective line items (multi or single). */
    public List<SellLineItemDto> resolveLineItems() {
        if (lineItems != null && !lineItems.isEmpty()) {
            return lineItems;
        }
        if (medicineId != null && quantity != null && quantity >= 1) {
            SellLineItemDto single = new SellLineItemDto();
            single.setMedicineId(medicineId);
            single.setQuantity(quantity);
            return List.of(single);
        }
        return List.of();
    }
}
