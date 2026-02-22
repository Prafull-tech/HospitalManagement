package com.hospital.hms.billing.dto;

import com.hospital.hms.billing.entity.BillingServiceType;
import com.hospital.hms.billing.entity.BillingItemStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class BillingItemResponseDto {

    private Long id;
    private Long billingAccountId;
    private BillingServiceType serviceType;
    private String serviceName;
    private Long referenceId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String department;
    private String createdBy;
    private BillingItemStatus status;
    private Instant createdAt;
    private BigDecimal gstPercent;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBillingAccountId() { return billingAccountId; }
    public void setBillingAccountId(Long billingAccountId) { this.billingAccountId = billingAccountId; }
    public BillingServiceType getServiceType() { return serviceType; }
    public void setServiceType(BillingServiceType serviceType) { this.serviceType = serviceType; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public BillingItemStatus getStatus() { return status; }
    public void setStatus(BillingItemStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public BigDecimal getGstPercent() { return gstPercent; }
    public void setGstPercent(BigDecimal gstPercent) { this.gstPercent = gstPercent; }
    public BigDecimal getCgst() { return cgst; }
    public void setCgst(BigDecimal cgst) { this.cgst = cgst; }
    public BigDecimal getSgst() { return sgst; }
    public void setSgst(BigDecimal sgst) { this.sgst = sgst; }
    public BigDecimal getIgst() { return igst; }
    public void setIgst(BigDecimal igst) { this.igst = igst; }
}
