package com.hospital.hms.billing.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Multi-visit OPD billing. Groups multiple OPD visits into one bill.
 */
@Entity
@Table(
    name = "opd_group_bills",
    indexes = {
        @Index(name = "idx_opd_group_patient", columnList = "patient_id"),
        @Index(name = "idx_opd_group_account", columnList = "billing_account_id")
    }
)
public class OpdGroupBill extends BaseIdEntity {

    @NotNull
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @NotNull
    @Column(name = "billing_account_id", nullable = false)
    private Long billingAccountId;

    /** Comma-separated visit IDs e.g. "1,2,3" */
    @NotNull
    @Column(name = "visit_ids", nullable = false, length = 500)
    private String visitIds;

    @NotNull
    @Column(name = "total_consultation_charges", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalConsultationCharges = BigDecimal.ZERO;

    public OpdGroupBill() {
    }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getBillingAccountId() { return billingAccountId; }
    public void setBillingAccountId(Long billingAccountId) { this.billingAccountId = billingAccountId; }
    public String getVisitIds() { return visitIds; }
    public void setVisitIds(String visitIds) { this.visitIds = visitIds; }
    public BigDecimal getTotalConsultationCharges() { return totalConsultationCharges; }
    public void setTotalConsultationCharges(BigDecimal totalConsultationCharges) { this.totalConsultationCharges = totalConsultationCharges; }
}
