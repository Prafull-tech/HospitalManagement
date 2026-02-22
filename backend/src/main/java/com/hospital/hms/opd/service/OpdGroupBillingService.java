package com.hospital.hms.opd.service;

import com.hospital.hms.billing.entity.BillingServiceType;
import com.hospital.hms.billing.entity.OpdGroupBill;
import com.hospital.hms.billing.entity.PatientBillingAccount;
import com.hospital.hms.billing.repository.BillingItemRepository;
import com.hospital.hms.billing.repository.OpdGroupBillRepository;
import com.hospital.hms.billing.repository.PatientBillingAccountRepository;
import com.hospital.hms.billing.service.BillingAccountService;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.opd.dto.OpdGroupBillingRequestDto;
import com.hospital.hms.opd.repository.OPDVisitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * Multi-visit OPD billing. Groups visits into one bill.
 */
@Service
public class OpdGroupBillingService {

    private static final Logger log = LoggerFactory.getLogger(OpdGroupBillingService.class);

    private final OPDVisitRepository opdVisitRepository;
    private final PatientBillingAccountRepository accountRepository;
    private final BillingItemRepository itemRepository;
    private final OpdGroupBillRepository opdGroupBillRepository;
    private final BillingAccountService billingAccountService;

    public OpdGroupBillingService(OPDVisitRepository opdVisitRepository,
                                  PatientBillingAccountRepository accountRepository,
                                  BillingItemRepository itemRepository,
                                  OpdGroupBillRepository opdGroupBillRepository,
                                  BillingAccountService billingAccountService) {
        this.opdVisitRepository = opdVisitRepository;
        this.accountRepository = accountRepository;
        this.itemRepository = itemRepository;
        this.opdGroupBillRepository = opdGroupBillRepository;
        this.billingAccountService = billingAccountService;
    }

    @Transactional
    public com.hospital.hms.billing.dto.BillingAccountViewDto createGroupBill(OpdGroupBillingRequestDto request) {
        if (request.getVisitIds().isEmpty()) {
            throw new IllegalArgumentException("At least one visit ID required");
        }

        var firstVisit = opdVisitRepository.findById(request.getVisitIds().get(0))
                .orElseThrow(() -> new ResourceNotFoundException("OPD visit not found: " + request.getVisitIds().get(0)));

        Long patientId = firstVisit.getPatient().getId();
        String uhid = firstVisit.getPatient().getUhid();

        PatientBillingAccount account = new PatientBillingAccount();
        account.setPatientId(patientId);
        account.setUhid(uhid);
        account.setOpdVisitId(request.getVisitIds().get(0));
        account.setBillStatus(com.hospital.hms.billing.entity.BillStatus.ACTIVE);
        account.setTotalAmount(BigDecimal.ZERO);
        account.setPaidAmount(BigDecimal.ZERO);
        account.setPendingAmount(BigDecimal.ZERO);
        account = accountRepository.save(account);

        BigDecimal chargePerVisit = request.getConsultationChargePerVisit();
        BigDecimal totalConsultation = BigDecimal.ZERO;
        String visitIdsStr = request.getVisitIds().stream().map(String::valueOf).collect(Collectors.joining(","));

        for (Long visitId : request.getVisitIds()) {
            var visit = opdVisitRepository.findById(visitId)
                    .orElseThrow(() -> new ResourceNotFoundException("OPD visit not found: " + visitId));
            if (!visit.getPatient().getId().equals(patientId)) {
                throw new IllegalArgumentException("All visits must be for same patient");
            }

            com.hospital.hms.billing.entity.BillingItem item = new com.hospital.hms.billing.entity.BillingItem();
            item.setBillingAccount(account);
            item.setServiceType(BillingServiceType.CONSULTATION);
            item.setServiceName("OPD Consultation - Visit " + visit.getVisitNumber());
            item.setReferenceId(visitId);
            item.setQuantity(1);
            item.setUnitPrice(chargePerVisit);
            item.setTotalPrice(chargePerVisit);
            item.setDepartment("OPD");
            item.setCreatedBy(SecurityContextUserResolver.resolveUserId());
            item.setStatus(com.hospital.hms.billing.entity.BillingItemStatus.POSTED);
            item.setCorrelationId(MDC.get(MdcKeys.CORRELATION_ID));
            itemRepository.save(item);

            totalConsultation = totalConsultation.add(chargePerVisit);
        }

        account.setTotalAmount(totalConsultation);
        account.setPendingAmount(totalConsultation);
        accountRepository.save(account);

        OpdGroupBill groupBill = new OpdGroupBill();
        groupBill.setPatientId(patientId);
        groupBill.setBillingAccountId(account.getId());
        groupBill.setVisitIds(visitIdsStr);
        groupBill.setTotalConsultationCharges(totalConsultation);
        opdGroupBillRepository.save(groupBill);

        log.info("OPD group bill created: account={} visits={} total={} correlationId={}",
                account.getId(), visitIdsStr, totalConsultation, MDC.get(MdcKeys.CORRELATION_ID));

        return billingAccountService.getAccountViewByBillingAccountId(account.getId());
    }
}
