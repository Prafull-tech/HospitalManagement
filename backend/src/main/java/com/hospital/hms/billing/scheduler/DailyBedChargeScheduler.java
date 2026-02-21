package com.hospital.hms.billing.scheduler;

import com.hospital.hms.billing.dto.AddBillingItemRequestDto;
import com.hospital.hms.billing.entity.BillingServiceType;
import com.hospital.hms.billing.entity.PatientBillingAccount;
import com.hospital.hms.billing.repository.BillingItemRepository;
import com.hospital.hms.billing.repository.PatientBillingAccountRepository;
import com.hospital.hms.billing.service.BillingEngine;
import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.entity.BedAllocation;
import com.hospital.hms.ipd.repository.BedAllocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Runs at midnight. For each admitted IPD patient, posts daily bed/room charge.
 */
@Component
public class DailyBedChargeScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyBedChargeScheduler.class);
    private static final BigDecimal DEFAULT_DAILY_RATE = new BigDecimal("500");

    private final BedAllocationRepository bedAllocationRepository;
    private final PatientBillingAccountRepository accountRepository;
    private final BillingItemRepository itemRepository;
    private final BillingEngine billingEngine;

    public DailyBedChargeScheduler(BedAllocationRepository bedAllocationRepository,
                                   PatientBillingAccountRepository accountRepository,
                                   BillingItemRepository itemRepository,
                                   BillingEngine billingEngine) {
        this.bedAllocationRepository = bedAllocationRepository;
        this.accountRepository = accountRepository;
        this.itemRepository = itemRepository;
        this.billingEngine = billingEngine;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void postDailyBedCharges() {
        LocalDate today = LocalDate.now();
        log.info("Daily bed charge job started for {}", today);

        List<BedAllocation> activeAllocations = bedAllocationRepository.findActiveWithAdmissionAndWard();
        int posted = 0;
        for (BedAllocation ba : activeAllocations) {
            var admission = ba.getAdmission();
            AdmissionStatus status = admission.getAdmissionStatus();
            if (status != AdmissionStatus.ADMITTED && status != AdmissionStatus.ACTIVE
                    && status != AdmissionStatus.TRANSFERRED && status != AdmissionStatus.DISCHARGE_INITIATED) {
                continue;
            }
            Long admissionId = admission.getId();
            PatientBillingAccount account = accountRepository.findByIpdAdmissionId(admissionId).orElse(null);
            if (account == null) continue;
            if (itemRepository.findBedChargeByAccountAndDate(account.getId(), today).isPresent()) continue;

            var ward = ba.getBed().getWard();
            BigDecimal rate = ward.getDailyChargePerBed() != null ? ward.getDailyChargePerBed() : DEFAULT_DAILY_RATE;
            AddBillingItemRequestDto req = new AddBillingItemRequestDto();
            req.setIpdAdmissionId(admissionId);
            req.setServiceType(BillingServiceType.BED);
            req.setServiceName("Room charge - " + ward.getName() + " (" + today + ")");
            req.setQuantity(1);
            req.setUnitPrice(rate);
            req.setDepartment("IPD");
            req.setChargeDate(today);
            billingEngine.addItem(req);
            posted++;
        }
        log.info("Daily bed charge job completed: {} charges posted", posted);
    }
}
