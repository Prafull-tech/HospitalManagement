package com.hospital.hms.enquiry.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnquiryDashboardDto {
    private long openCount;
    private long inProgressCount;
    private long resolvedCount;
    private long closedCount;
    private long escalatedCount;
    private List<EnquiryResponseDto> recentEnquiries = new ArrayList<>();
    private Map<String, Long> byCategory;

    public long getOpenCount() {
        return openCount;
    }

    public void setOpenCount(long openCount) {
        this.openCount = openCount;
    }

    public long getInProgressCount() {
        return inProgressCount;
    }

    public void setInProgressCount(long inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    public long getResolvedCount() {
        return resolvedCount;
    }

    public void setResolvedCount(long resolvedCount) {
        this.resolvedCount = resolvedCount;
    }

    public long getClosedCount() {
        return closedCount;
    }

    public void setClosedCount(long closedCount) {
        this.closedCount = closedCount;
    }

    public long getEscalatedCount() {
        return escalatedCount;
    }

    public void setEscalatedCount(long escalatedCount) {
        this.escalatedCount = escalatedCount;
    }

    public List<EnquiryResponseDto> getRecentEnquiries() {
        return recentEnquiries;
    }

    public void setRecentEnquiries(List<EnquiryResponseDto> recentEnquiries) {
        this.recentEnquiries = recentEnquiries;
    }

    public Map<String, Long> getByCategory() {
        return byCategory;
    }

    public void setByCategory(Map<String, Long> byCategory) {
        this.byCategory = byCategory;
    }
}
