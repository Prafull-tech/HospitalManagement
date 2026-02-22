package com.hospital.hms.billing.dto;

import com.hospital.hms.billing.entity.BillStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Full billing account view for dashboard /billing/account/{ipdId}.
 */
public class BillingAccountViewDto {

    private Long id;
    private Long patientId;
    private String uhid;
    private String patientName;
    private Long ipdAdmissionId;
    private String admissionNumber;
    private Long opdVisitId;
    private BillStatus billStatus;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private String insuranceType;
    private String tpaApprovalStatus;

    private Map<com.hospital.hms.billing.entity.BillingServiceType, BigDecimal> totalByServiceType;
    private List<BillingItemResponseDto> items;

    private Boolean corporate;
    private Boolean corporateApproved;
    private Boolean emiActive;
    private Boolean hasGstSplit;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getUhid() { return uhid; }
    public void setUhid(String uhid) { this.uhid = uhid; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public Long getIpdAdmissionId() { return ipdAdmissionId; }
    public void setIpdAdmissionId(Long ipdAdmissionId) { this.ipdAdmissionId = ipdAdmissionId; }
    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }
    public Long getOpdVisitId() { return opdVisitId; }
    public void setOpdVisitId(Long opdVisitId) { this.opdVisitId = opdVisitId; }
    public BillStatus getBillStatus() { return billStatus; }
    public void setBillStatus(BillStatus billStatus) { this.billStatus = billStatus; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public BigDecimal getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(BigDecimal pendingAmount) { this.pendingAmount = pendingAmount; }
    public String getInsuranceType() { return insuranceType; }
    public void setInsuranceType(String insuranceType) { this.insuranceType = insuranceType; }
    public String getTpaApprovalStatus() { return tpaApprovalStatus; }
    public void setTpaApprovalStatus(String tpaApprovalStatus) { this.tpaApprovalStatus = tpaApprovalStatus; }
    public Map<com.hospital.hms.billing.entity.BillingServiceType, BigDecimal> getTotalByServiceType() { return totalByServiceType; }
    public void setTotalByServiceType(Map<com.hospital.hms.billing.entity.BillingServiceType, BigDecimal> totalByServiceType) { this.totalByServiceType = totalByServiceType; }
    public List<BillingItemResponseDto> getItems() { return items; }
    public void setItems(List<BillingItemResponseDto> items) { this.items = items; }
    public Boolean getCorporate() { return corporate; }
    public void setCorporate(Boolean corporate) { this.corporate = corporate; }
    public Boolean getCorporateApproved() { return corporateApproved; }
    public void setCorporateApproved(Boolean corporateApproved) { this.corporateApproved = corporateApproved; }
    public Boolean getEmiActive() { return emiActive; }
    public void setEmiActive(Boolean emiActive) { this.emiActive = emiActive; }
    public Boolean getHasGstSplit() { return hasGstSplit; }
    public void setHasGstSplit(Boolean hasGstSplit) { this.hasGstSplit = hasGstSplit; }
}
