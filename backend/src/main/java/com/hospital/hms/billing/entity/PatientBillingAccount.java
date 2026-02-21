package com.hospital.hms.billing.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Centralized billing account for a patient visit (IPD or OPD).
 * Aggregates total, paid, pending. NABH / Insurance compliant.
 */
@Entity
@Table(
    name = "patient_billing_accounts",
    indexes = {
        @Index(name = "idx_billing_account_patient", columnList = "patient_id"),
        @Index(name = "idx_billing_account_uhid", columnList = "uhid"),
        @Index(name = "idx_billing_account_ipd", columnList = "ipd_admission_id"),
        @Index(name = "idx_billing_account_opd", columnList = "opd_visit_id"),
        @Index(name = "idx_billing_account_status", columnList = "bill_status")
    }
)
public class PatientBillingAccount extends BaseIdEntity {

    @NotNull
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @NotNull
    @Size(max = 50)
    @Column(name = "uhid", nullable = false, length = 50)
    private String uhid;

    @Column(name = "ipd_admission_id")
    private Long ipdAdmissionId;

    @Column(name = "opd_visit_id")
    private Long opdVisitId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "bill_status", nullable = false, length = 20)
    private BillStatus billStatus = BillStatus.ACTIVE;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "pending_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal pendingAmount = BigDecimal.ZERO;

    @Size(max = 50)
    @Column(name = "insurance_type", length = 50)
    private String insuranceType;

    @Size(max = 30)
    @Column(name = "tpa_approval_status", length = 30)
    private String tpaApprovalStatus;

    public PatientBillingAccount() {
    }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getUhid() { return uhid; }
    public void setUhid(String uhid) { this.uhid = uhid; }
    public Long getIpdAdmissionId() { return ipdAdmissionId; }
    public void setIpdAdmissionId(Long ipdAdmissionId) { this.ipdAdmissionId = ipdAdmissionId; }
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
}
