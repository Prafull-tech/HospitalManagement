package com.hospital.hms.superadmin.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(
    name = "subscription_plans",
    indexes = {
        @Index(name = "idx_plan_code", columnList = "plan_code", unique = true),
        @Index(name = "idx_plan_active", columnList = "is_active")
    }
)
public class SubscriptionPlan extends BaseIdEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "plan_code", nullable = false, unique = true, length = 50)
    private String planCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "quarterly_price", precision = 10, scale = 2)
    private BigDecimal quarterlyPrice;

    @Column(name = "yearly_price", precision = 10, scale = 2)
    private BigDecimal yearlyPrice;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_beds")
    private Integer maxBeds;

    @Size(max = 1000)
    @Column(name = "enabled_modules", length = 1000)
    private String enabledModules;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "trial_days")
    private Integer trialDays;

    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getMonthlyPrice() { return monthlyPrice; }
    public void setMonthlyPrice(BigDecimal monthlyPrice) { this.monthlyPrice = monthlyPrice; }

    public BigDecimal getQuarterlyPrice() { return quarterlyPrice; }
    public void setQuarterlyPrice(BigDecimal quarterlyPrice) { this.quarterlyPrice = quarterlyPrice; }

    public BigDecimal getYearlyPrice() { return yearlyPrice; }
    public void setYearlyPrice(BigDecimal yearlyPrice) { this.yearlyPrice = yearlyPrice; }

    public Integer getMaxUsers() { return maxUsers; }
    public void setMaxUsers(Integer maxUsers) { this.maxUsers = maxUsers; }

    public Integer getMaxBeds() { return maxBeds; }
    public void setMaxBeds(Integer maxBeds) { this.maxBeds = maxBeds; }

    public String getEnabledModules() { return enabledModules; }
    public void setEnabledModules(String enabledModules) { this.enabledModules = enabledModules; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getTrialDays() { return trialDays; }
    public void setTrialDays(Integer trialDays) { this.trialDays = trialDays; }
}
