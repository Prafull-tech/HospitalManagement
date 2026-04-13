package com.hospital.hms.superadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class SubscriptionPlanRequestDto {

    @NotBlank(message = "Plan code is required")
    @Size(max = 50)
    private String planCode;

    @NotBlank(message = "Plan name is required")
    @Size(max = 100)
    private String planName;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Monthly price is required")
    private BigDecimal monthlyPrice;

    private BigDecimal quarterlyPrice;
    private BigDecimal yearlyPrice;
    private Integer maxUsers;
    private Integer maxBeds;
    private String enabledModules;
    private Boolean active = true;
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

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Integer getTrialDays() { return trialDays; }
    public void setTrialDays(Integer trialDays) { this.trialDays = trialDays; }
}
