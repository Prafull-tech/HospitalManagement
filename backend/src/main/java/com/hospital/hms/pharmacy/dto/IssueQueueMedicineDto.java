package com.hospital.hms.pharmacy.dto;

/**
 * Medicine line in the issue queue (grouped by patient).
 */
public class IssueQueueMedicineDto {

    private Long orderId;
    private Long medicineId;
    private String medicineName;
    private int quantity;
    private String dosage;
    private String route;
    private boolean lasa;
    private BatchSuggestionDto fefoSuggestion;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public boolean isLasa() {
        return lasa;
    }

    public void setLasa(boolean lasa) {
        this.lasa = lasa;
    }

    public BatchSuggestionDto getFefoSuggestion() {
        return fefoSuggestion;
    }

    public void setFefoSuggestion(BatchSuggestionDto fefoSuggestion) {
        this.fefoSuggestion = fefoSuggestion;
    }
}
