package com.hospital.hms.billing.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Individual billing line item. Auto-generated from Pharmacy, Lab, OT, Bed, etc.
 * NABH / medico-legal audit-ready.
 */
@Entity
@Table(
    name = "billing_items",
    indexes = {
        @Index(name = "idx_billing_item_account", columnList = "billing_account_id"),
        @Index(name = "idx_billing_item_service", columnList = "service_type"),
        @Index(name = "idx_billing_item_reference", columnList = "reference_id"),
        @Index(name = "idx_billing_item_created", columnList = "created_at")
    }
)
public class BillingItem extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_account_id", nullable = false)
    private PatientBillingAccount billingAccount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private BillingServiceType serviceType;

    @NotNull
    @Size(max = 255)
    @Column(name = "service_name", nullable = false, length = 255)
    private String serviceName;

    @Column(name = "reference_id")
    private Long referenceId;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @NotNull
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Size(max = 50)
    @Column(name = "department", length = 50)
    private String department;

    @Size(max = 255)
    @Column(name = "created_by", length = 255)
    private String createdBy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BillingItemStatus status = BillingItemStatus.POSTED;

    @Size(max = 100)
    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "charge_date")
    private LocalDate chargeDate;

    public BillingItem() {
    }

    public PatientBillingAccount getBillingAccount() { return billingAccount; }
    public void setBillingAccount(PatientBillingAccount billingAccount) { this.billingAccount = billingAccount; }
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
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public LocalDate getChargeDate() { return chargeDate; }
    public void setChargeDate(LocalDate chargeDate) { this.chargeDate = chargeDate; }
}
