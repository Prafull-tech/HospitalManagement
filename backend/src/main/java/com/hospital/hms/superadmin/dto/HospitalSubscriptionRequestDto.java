package com.hospital.hms.superadmin.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class HospitalSubscriptionRequestDto {

    @NotNull(message = "Hospital ID is required")
    private Long hospitalId;

    @NotNull(message = "Plan ID is required")
    private Long planId;

    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate trialEndDate;
    private String billingCycle;
    private String notes;

    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }

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
