package com.hospital.hms.billing.dto;

import com.hospital.hms.billing.entity.BillingServiceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request for POST /api/billing/add-item. Event-based charge capture.
 */
public class AddBillingItemRequestDto {

    private Long ipdAdmissionId;
    private Long opdVisitId;

    @NotNull
    private BillingServiceType serviceType;

    @NotNull
    @Size(max = 255)
    private String serviceName;

    private Long referenceId;

    @NotNull
    @DecimalMin("0")
    private Integer quantity = 1;

    @NotNull
    @DecimalMin("0")
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Size(max = 50)
    private String department;

    private LocalDate chargeDate;

    public Long getIpdAdmissionId() { return ipdAdmissionId; }
    public void setIpdAdmissionId(Long ipdAdmissionId) { this.ipdAdmissionId = ipdAdmissionId; }
    public Long getOpdVisitId() { return opdVisitId; }
    public void setOpdVisitId(Long opdVisitId) { this.opdVisitId = opdVisitId; }
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
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public LocalDate getChargeDate() { return chargeDate; }
    public void setChargeDate(LocalDate chargeDate) { this.chargeDate = chargeDate; }
}
