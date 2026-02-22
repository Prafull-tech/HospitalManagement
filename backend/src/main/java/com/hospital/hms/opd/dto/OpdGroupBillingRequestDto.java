package com.hospital.hms.opd.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request for POST /api/opd/billing/group.
 */
public class OpdGroupBillingRequestDto {

    @NotEmpty
    private List<Long> visitIds;

    @NotNull
    private java.math.BigDecimal consultationChargePerVisit;

    public List<Long> getVisitIds() { return visitIds; }
    public void setVisitIds(List<Long> visitIds) { this.visitIds = visitIds; }
    public java.math.BigDecimal getConsultationChargePerVisit() { return consultationChargePerVisit; }
    public void setConsultationChargePerVisit(java.math.BigDecimal consultationChargePerVisit) { this.consultationChargePerVisit = consultationChargePerVisit; }
}
