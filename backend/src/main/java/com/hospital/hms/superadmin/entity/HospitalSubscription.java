package com.hospital.hms.superadmin.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.hospital.entity.Hospital;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(
    name = "hospital_subscriptions",
    indexes = {
        @Index(name = "idx_hsub_hospital", columnList = "hospital_id"),
        @Index(name = "idx_hsub_plan", columnList = "plan_id"),
        @Index(name = "idx_hsub_status", columnList = "status")
    }
)
public class HospitalSubscription extends BaseIdEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SubscriptionStatus status = SubscriptionStatus.TRIAL;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "trial_end_date")
    private LocalDate trialEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", length = 20)
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    @Column(name = "notes", length = 500)
    private String notes;

    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = hospital; }

    public SubscriptionPlan getPlan() { return plan; }
    public void setPlan(SubscriptionPlan plan) { this.plan = plan; }

    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getTrialEndDate() { return trialEndDate; }
    public void setTrialEndDate(LocalDate trialEndDate) { this.trialEndDate = trialEndDate; }

    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
