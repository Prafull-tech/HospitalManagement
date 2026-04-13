package com.hospital.hms.superadmin.dto;

import java.time.LocalDate;

public class HospitalSubscriptionResponseDto {
    private Long id;
    private Long hospitalId;
    private String hospitalName;
    private String hospitalCode;
    private Long planId;
    private String planName;
    private String planCode;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate trialEndDate;
    private String billingCycle;
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getHospitalCode() { return hospitalCode; }
    public void setHospitalCode(String hospitalCode) { this.hospitalCode = hospitalCode; }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getTrialEndDate() { return trialEndDate; }
    public void setTrialEndDate(LocalDate trialEndDate) { this.trialEndDate = trialEndDate; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
