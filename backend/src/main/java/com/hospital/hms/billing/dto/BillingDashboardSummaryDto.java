package com.hospital.hms.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** KPIs for billing dashboard (date defaults to today on server). */
public class BillingDashboardSummaryDto {

    private LocalDate date;
    private BigDecimal todayCollection;
    private long paymentCountToday;
    private BigDecimal totalPendingActiveAccounts;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getTodayCollection() {
        return todayCollection;
    }

    public void setTodayCollection(BigDecimal todayCollection) {
        this.todayCollection = todayCollection;
    }

    public long getPaymentCountToday() {
        return paymentCountToday;
    }

    public void setPaymentCountToday(long paymentCountToday) {
        this.paymentCountToday = paymentCountToday;
    }

    public BigDecimal getTotalPendingActiveAccounts() {
        return totalPendingActiveAccounts;
    }

    public void setTotalPendingActiveAccounts(BigDecimal totalPendingActiveAccounts) {
        this.totalPendingActiveAccounts = totalPendingActiveAccounts;
    }
}
