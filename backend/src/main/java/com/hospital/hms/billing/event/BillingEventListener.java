package com.hospital.hms.billing.event;

import com.hospital.hms.billing.service.BillingAccountService;
import com.hospital.hms.common.event.OpdVisitCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class BillingEventListener {

    private static final Logger log = LoggerFactory.getLogger(BillingEventListener.class);

    private final BillingAccountService billingAccountService;

    public BillingEventListener(BillingAccountService billingAccountService) {
        this.billingAccountService = billingAccountService;
    }

    @Async
    @EventListener
    public void onOpdVisitCompleted(OpdVisitCompletedEvent event) {
        log.info("Received OpdVisitCompleted event for visitId={}, posting consultation fee",
                event.getOpdVisitId());
        try {
            billingAccountService.postOpdConsultationFeeIfAbsent(
                    event.getOpdVisitId(),
                    event.getConsultationFee(),
                    event.getDoctorDisplayName());
        } catch (Exception ex) {
            log.error("Failed to post consultation fee for visitId={}: {}",
                    event.getOpdVisitId(), ex.getMessage(), ex);
        }
    }
}
