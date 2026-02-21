package com.hospital.hms.billing.service;

import com.hospital.hms.billing.dto.AddBillingItemRequestDto;
import com.hospital.hms.billing.entity.*;
import com.hospital.hms.billing.repository.AdmissionChargeRepository;
import com.hospital.hms.billing.repository.BillingItemRepository;
import com.hospital.hms.billing.repository.PatientBillingAccountRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.opd.repository.OPDVisitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Centralized billing engine. Adds items to PatientBillingAccount and BillingItem.
 * Also syncs to AdmissionCharge for backward compatibility with IPD views.
 */
@Service
public class BillingEngine {

    private static final Logger log = LoggerFactory.getLogger(BillingEngine.class);

    private final PatientBillingAccountRepository accountRepository;
    private final BillingItemRepository itemRepository;
    private final AdmissionChargeRepository chargeRepository;
    private final IPDAdmissionRepository admissionRepository;
    private final OPDVisitRepository opdVisitRepository;

    public BillingEngine(PatientBillingAccountRepository accountRepository,
                        BillingItemRepository itemRepository,
                        AdmissionChargeRepository chargeRepository,
                        IPDAdmissionRepository admissionRepository,
                        OPDVisitRepository opdVisitRepository) {
        this.accountRepository = accountRepository;
        this.itemRepository = itemRepository;
        this.chargeRepository = chargeRepository;
        this.admissionRepository = admissionRepository;
        this.opdVisitRepository = opdVisitRepository;
    }

    /**
     * Add a billing item. Creates account if needed. Updates totals. Syncs to AdmissionCharge for IPD.
     */
    @Transactional
    public BillingItem addItem(AddBillingItemRequestDto request) {
        if (request.getIpdAdmissionId() == null && request.getOpdVisitId() == null) {
            throw new IllegalArgumentException("Either ipdAdmissionId or opdVisitId must be provided");
        }

        BigDecimal totalPrice = request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        String user = SecurityContextUserResolver.resolveUserId();
        String correlationId = java.util.Optional.ofNullable(org.slf4j.MDC.get(MdcKeys.CORRELATION_ID))
                .orElse("BILL-" + UUID.randomUUID());

        PatientBillingAccount account = getOrCreateAccount(request.getIpdAdmissionId(), request.getOpdVisitId());
        BillingItem item = new BillingItem();
        item.setBillingAccount(account);
        item.setServiceType(request.getServiceType());
        item.setServiceName(request.getServiceName());
        item.setReferenceId(request.getReferenceId());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setTotalPrice(totalPrice);
        item.setDepartment(request.getDepartment());
        item.setCreatedBy(user);
        item.setStatus(BillingItemStatus.POSTED);
        item.setCorrelationId(correlationId);
        if (request.getChargeDate() != null) {
            item.setChargeDate(request.getChargeDate());
        }
        item = itemRepository.save(item);

        account.setTotalAmount(account.getTotalAmount().add(totalPrice));
        account.setPendingAmount(account.getPendingAmount().add(totalPrice));
        accountRepository.save(account);

        if (request.getIpdAdmissionId() != null) {
            IPDAdmission admission = admissionRepository.findById(request.getIpdAdmissionId())
                    .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + request.getIpdAdmissionId()));
            com.hospital.hms.billing.entity.AdmissionCharge charge = new com.hospital.hms.billing.entity.AdmissionCharge();
            charge.setIpdAdmission(admission);
            charge.setChargeType(toChargeType(request.getServiceType()));
            charge.setAmount(totalPrice);
            charge.setDescription(request.getServiceName());
            charge.setReferenceType(request.getServiceType().name());
            charge.setReferenceId(request.getReferenceId());
            chargeRepository.save(charge);
        }

        log.info("Billing item added: account={} service={} amount={} ref={} by={}",
                account.getId(), request.getServiceType(), totalPrice, request.getReferenceId(), user);
        return item;
    }

    private PatientBillingAccount getOrCreateAccount(Long ipdAdmissionId, Long opdVisitId) {
        if (ipdAdmissionId != null) {
            return accountRepository.findByIpdAdmissionId(ipdAdmissionId)
                    .orElseGet(() -> createAccountForIpd(ipdAdmissionId));
        }
        return accountRepository.findByOpdVisitId(opdVisitId)
                .orElseGet(() -> createAccountForOpd(opdVisitId));
    }

    private PatientBillingAccount createAccountForIpd(Long ipdAdmissionId) {
        var admission = admissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId));
        PatientBillingAccount acc = new PatientBillingAccount();
        acc.setPatientId(admission.getPatient().getId());
        acc.setUhid(admission.getPatient().getUhid());
        acc.setIpdAdmissionId(ipdAdmissionId);
        acc.setBillStatus(BillStatus.ACTIVE);
        acc.setTotalAmount(BigDecimal.ZERO);
        acc.setPaidAmount(BigDecimal.ZERO);
        acc.setPendingAmount(BigDecimal.ZERO);
        acc.setInsuranceType(admission.getInsuranceTpa());
        return accountRepository.save(acc);
    }

    private PatientBillingAccount createAccountForOpd(Long opdVisitId) {
        var visit = opdVisitRepository.findById(opdVisitId)
                .orElseThrow(() -> new ResourceNotFoundException("OPD visit not found: " + opdVisitId));
        PatientBillingAccount acc = new PatientBillingAccount();
        acc.setPatientId(visit.getPatient().getId());
        acc.setUhid(visit.getPatient().getUhid());
        acc.setOpdVisitId(opdVisitId);
        acc.setBillStatus(BillStatus.ACTIVE);
        acc.setTotalAmount(BigDecimal.ZERO);
        acc.setPaidAmount(BigDecimal.ZERO);
        acc.setPendingAmount(BigDecimal.ZERO);
        return accountRepository.save(acc);
    }

    private static ChargeType toChargeType(BillingServiceType st) {
        return switch (st) {
            case BED, OTHER -> ChargeType.OTHER;
            case PHARMACY -> ChargeType.PHARMACY;
            case LAB -> ChargeType.LAB;
            case OT, PROCEDURE -> ChargeType.PROCEDURE;
            case RADIOLOGY -> ChargeType.RADIOLOGY;
            case CONSULTATION -> ChargeType.CONSULTATION;
            case NURSING -> ChargeType.NURSING;
            case BLOOD_BANK -> ChargeType.BLOOD_BANK;
            case PHYSIOTHERAPY -> ChargeType.PHYSIOTHERAPY;
            case EMERGENCY -> ChargeType.EMERGENCY;
        };
    }
}
