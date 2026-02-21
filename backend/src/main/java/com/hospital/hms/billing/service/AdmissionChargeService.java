package com.hospital.hms.billing.service;

import com.hospital.hms.billing.dto.AdmissionChargeRequestDto;
import com.hospital.hms.billing.entity.*;
import com.hospital.hms.billing.repository.BillingItemRepository;
import com.hospital.hms.billing.repository.PatientBillingAccountRepository;
import com.hospital.hms.billing.repository.AdmissionChargeRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.opd.repository.OPDVisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Charges auto-added to billing by Pharmacy, Lab, Doctor Orders, etc.
 * Creates both AdmissionCharge (legacy) and BillingItem (new engine).
 */
@Service
public class AdmissionChargeService {

    private final AdmissionChargeRepository chargeRepository;
    private final IPDAdmissionRepository admissionRepository;
    private final PatientBillingAccountRepository accountRepository;
    private final BillingItemRepository itemRepository;
    private final OPDVisitRepository opdVisitRepository;

    public AdmissionChargeService(AdmissionChargeRepository chargeRepository,
                                  IPDAdmissionRepository admissionRepository,
                                  PatientBillingAccountRepository accountRepository,
                                  BillingItemRepository itemRepository,
                                  OPDVisitRepository opdVisitRepository) {
        this.chargeRepository = chargeRepository;
        this.admissionRepository = admissionRepository;
        this.accountRepository = accountRepository;
        this.itemRepository = itemRepository;
        this.opdVisitRepository = opdVisitRepository;
    }

    /**
     * Add a charge line for an IPD admission. Creates AdmissionCharge + BillingItem.
     */
    @Transactional
    public AdmissionCharge addCharge(Long ipdAdmissionId, AdmissionChargeRequestDto request) {
        var admission = admissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId));
        AdmissionCharge charge = new AdmissionCharge();
        charge.setIpdAdmission(admission);
        charge.setChargeType(request.getChargeType());
        charge.setAmount(request.getAmount());
        charge.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        charge.setReferenceType(request.getReferenceType() != null ? request.getReferenceType().trim() : null);
        charge.setReferenceId(request.getReferenceId());
        charge = chargeRepository.save(charge);

        PatientBillingAccount account = accountRepository.findByIpdAdmissionId(ipdAdmissionId)
                .orElseGet(() -> createAccountForIpd(admission));
        BillingItem item = new BillingItem();
        item.setBillingAccount(account);
        item.setServiceType(toBillingServiceType(request.getChargeType()));
        item.setServiceName(request.getDescription() != null ? request.getDescription() : request.getChargeType().name());
        item.setReferenceId(request.getReferenceId());
        item.setQuantity(1);
        item.setUnitPrice(request.getAmount());
        item.setTotalPrice(request.getAmount());
        item.setDepartment(request.getChargeType().name());
        item.setCreatedBy(SecurityContextUserResolver.resolveUserId());
        item.setStatus(BillingItemStatus.POSTED);
        itemRepository.save(item);
        account.setTotalAmount(account.getTotalAmount().add(request.getAmount()));
        account.setPendingAmount(account.getPendingAmount().add(request.getAmount()));
        accountRepository.save(account);

        return charge;
    }

    private PatientBillingAccount createAccountForIpd(com.hospital.hms.ipd.entity.IPDAdmission admission) {
        PatientBillingAccount acc = new PatientBillingAccount();
        acc.setPatientId(admission.getPatient().getId());
        acc.setUhid(admission.getPatient().getUhid());
        acc.setIpdAdmissionId(admission.getId());
        acc.setBillStatus(BillStatus.ACTIVE);
        acc.setTotalAmount(BigDecimal.ZERO);
        acc.setPaidAmount(BigDecimal.ZERO);
        acc.setPendingAmount(BigDecimal.ZERO);
        acc.setInsuranceType(admission.getInsuranceTpa());
        return accountRepository.save(acc);
    }

    private static BillingServiceType toBillingServiceType(ChargeType ct) {
        return switch (ct) {
            case PHARMACY -> BillingServiceType.PHARMACY;
            case LAB -> BillingServiceType.LAB;
            case CONSULTATION -> BillingServiceType.CONSULTATION;
            case NURSING -> BillingServiceType.NURSING;
            case PROCEDURE, OT -> BillingServiceType.PROCEDURE;
            case RADIOLOGY -> BillingServiceType.RADIOLOGY;
            case BLOOD_BANK -> BillingServiceType.BLOOD_BANK;
            case PHYSIOTHERAPY -> BillingServiceType.PHYSIOTHERAPY;
            case EMERGENCY -> BillingServiceType.EMERGENCY;
            default -> BillingServiceType.OTHER;
        };
    }

    @Transactional(readOnly = true)
    public List<AdmissionCharge> findByIpdAdmissionId(Long ipdAdmissionId) {
        if (!admissionRepository.existsById(ipdAdmissionId)) {
            throw new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId);
        }
        return chargeRepository.findByIpdAdmissionIdOrderByCreatedAtDesc(ipdAdmissionId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalForIpdAdmission(Long ipdAdmissionId) {
        List<AdmissionCharge> charges = findByIpdAdmissionId(ipdAdmissionId);
        return charges.stream()
                .map(AdmissionCharge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
