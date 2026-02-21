package com.hospital.hms.pharmacy.dto;

import java.util.List;

/**
 * Patient-level item in the pharmacy issue queue (grouped from MedicationOrders).
 */
public class IssueQueuePatientDto {

    private String patientName;
    private String uhid;
    private String ipdNo;
    private String opdVisitNo;
    private String wardType;
    private String bed;
    private String priority;
    private List<IssueQueueMedicineDto> medicines;
    private List<Long> orderIds; // For issue action

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getUhid() {
        return uhid;
    }

    public void setUhid(String uhid) {
        this.uhid = uhid;
    }

    public String getIpdNo() {
        return ipdNo;
    }

    public void setIpdNo(String ipdNo) {
        this.ipdNo = ipdNo;
    }

    public String getOpdVisitNo() {
        return opdVisitNo;
    }

    public void setOpdVisitNo(String opdVisitNo) {
        this.opdVisitNo = opdVisitNo;
    }

    public String getWardType() {
        return wardType;
    }

    public void setWardType(String wardType) {
        this.wardType = wardType;
    }

    public String getBed() {
        return bed;
    }

    public void setBed(String bed) {
        this.bed = bed;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<IssueQueueMedicineDto> getMedicines() {
        return medicines;
    }

    public void setMedicines(List<IssueQueueMedicineDto> medicines) {
        this.medicines = medicines;
    }

    public List<Long> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<Long> orderIds) {
        this.orderIds = orderIds;
    }
}
