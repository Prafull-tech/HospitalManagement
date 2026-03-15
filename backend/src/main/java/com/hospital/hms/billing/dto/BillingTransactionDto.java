package com.hospital.hms.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** DTO for payment/transaction list (reception dashboard). */
public class BillingTransactionDto {
    private Long id;
    private Long ipdAdmissionId;
    private String admissionNumber;
    private String patientName;
    private String patientUhid;
    private String service;
    private BigDecimal amount;
    private String mode;
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIpdAdmissionId() { return ipdAdmissionId; }
    public void setIpdAdmissionId(Long ipdAdmissionId) { this.ipdAdmissionId = ipdAdmissionId; }
    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String s) { this.admissionNumber = s; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String s) { this.patientName = s; }
    public String getPatientUhid() { return patientUhid; }
    public void setPatientUhid(String s) { this.patientUhid = s; }
    public String getService() { return service; }
    public void setService(String s) { this.service = s; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal a) { this.amount = a; }
    public String getMode() { return mode; }
    public void setMode(String s) { this.mode = s; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant i) { this.createdAt = i; }
}
