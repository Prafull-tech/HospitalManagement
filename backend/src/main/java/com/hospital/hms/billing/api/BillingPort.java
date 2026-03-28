package com.hospital.hms.billing.api;

import java.math.BigDecimal;

/**
 * Port interface for billing operations — consumed by other modules (OPD, IPD, Pharmacy, Lab).
 * Decouples callers from billing internals.
 */
public interface BillingPort {

    void postCharge(Long billingAccountId, String serviceType, String serviceName,
                    Long referenceId, int quantity, BigDecimal unitPrice, String department);

    void postOpdConsultationFee(Long opdVisitId, BigDecimal unitPrice, String doctorDisplayName);

    Long getOrCreateAccountForIpd(Long ipdAdmissionId);

    Long getOrCreateAccountForOpd(Long opdVisitId);
}
