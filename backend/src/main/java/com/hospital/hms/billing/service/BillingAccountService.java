package com.hospital.hms.billing.service;

import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.billing.dto.BillingItemResponseDto;
import com.hospital.hms.billing.entity.BillingItem;
import com.hospital.hms.billing.entity.BillingServiceType;
import com.hospital.hms.billing.entity.PatientBillingAccount;
import com.hospital.hms.billing.repository.BillingItemRepository;
import com.hospital.hms.billing.repository.PatientBillingAccountRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Billing account view and finalize. Used by dashboard and discharge.
 */
@Service
public class BillingAccountService {

    private final PatientBillingAccountRepository accountRepository;
    private final BillingItemRepository itemRepository;
    private final IPDAdmissionRepository admissionRepository;

    public BillingAccountService(PatientBillingAccountRepository accountRepository,
                                 BillingItemRepository itemRepository,
                                 IPDAdmissionRepository admissionRepository) {
        this.accountRepository = accountRepository;
        this.itemRepository = itemRepository;
        this.admissionRepository = admissionRepository;
    }

    @Transactional
    public BillingAccountViewDto getAccountViewByIpdAdmissionId(Long ipdAdmissionId) {
        PatientBillingAccount account = accountRepository.findByIpdAdmissionId(ipdAdmissionId)
                .orElseGet(() -> createAccountForIpd(ipdAdmissionId));

        var admission = admissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId));

        List<BillingItem> items = itemRepository.findPostedByBillingAccountId(account.getId());
        Map<BillingServiceType, BigDecimal> byType = new HashMap<>();
        for (BillingItem bi : items) {
            byType.merge(bi.getServiceType(), bi.getTotalPrice(), BigDecimal::add);
        }

        BillingAccountViewDto dto = new BillingAccountViewDto();
        dto.setId(account.getId());
        dto.setPatientId(account.getPatientId());
        dto.setUhid(account.getUhid());
        dto.setPatientName(admission.getPatient().getFullName());
        dto.setIpdAdmissionId(account.getIpdAdmissionId());
        dto.setAdmissionNumber(admission.getAdmissionNumber());
        dto.setOpdVisitId(account.getOpdVisitId());
        dto.setBillStatus(account.getBillStatus());
        dto.setTotalAmount(account.getTotalAmount());
        dto.setPaidAmount(account.getPaidAmount());
        dto.setPendingAmount(account.getPendingAmount());
        dto.setInsuranceType(account.getInsuranceType());
        dto.setTpaApprovalStatus(account.getTpaApprovalStatus());
        dto.setTotalByServiceType(byType);
        dto.setItems(items.stream().map(this::toItemDto).collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public BillingAccountViewDto recordPayment(Long ipdAdmissionId, BigDecimal amount) {
        PatientBillingAccount account = accountRepository.findByIpdAdmissionId(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing account not found for IPD: " + ipdAdmissionId));

        account.setPaidAmount(account.getPaidAmount().add(amount));
        account.setPendingAmount(account.getPendingAmount().subtract(amount));
        if (account.getPendingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            account.setBillStatus(com.hospital.hms.billing.entity.BillStatus.CLOSED);
        }
        accountRepository.save(account);
        return getAccountViewByIpdAdmissionId(ipdAdmissionId);
    }

    private PatientBillingAccount createAccountForIpd(Long ipdAdmissionId) {
        var admission = admissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId));
        PatientBillingAccount acc = new PatientBillingAccount();
        acc.setPatientId(admission.getPatient().getId());
        acc.setUhid(admission.getPatient().getUhid());
        acc.setIpdAdmissionId(ipdAdmissionId);
        acc.setBillStatus(com.hospital.hms.billing.entity.BillStatus.ACTIVE);
        acc.setTotalAmount(BigDecimal.ZERO);
        acc.setPaidAmount(BigDecimal.ZERO);
        acc.setPendingAmount(BigDecimal.ZERO);
        acc.setInsuranceType(admission.getInsuranceTpa());
        return accountRepository.save(acc);
    }

    @Transactional(readOnly = true)
    public boolean isPaid(Long ipdAdmissionId) {
        return accountRepository.findByIpdAdmissionId(ipdAdmissionId)
                .map(a -> a.getPendingAmount().compareTo(BigDecimal.ZERO) <= 0)
                .orElse(true);
    }

    private BillingItemResponseDto toItemDto(BillingItem bi) {
        BillingItemResponseDto dto = new BillingItemResponseDto();
        dto.setId(bi.getId());
        dto.setBillingAccountId(bi.getBillingAccount().getId());
        dto.setServiceType(bi.getServiceType());
        dto.setServiceName(bi.getServiceName());
        dto.setReferenceId(bi.getReferenceId());
        dto.setQuantity(bi.getQuantity());
        dto.setUnitPrice(bi.getUnitPrice());
        dto.setTotalPrice(bi.getTotalPrice());
        dto.setDepartment(bi.getDepartment());
        dto.setCreatedBy(bi.getCreatedBy());
        dto.setStatus(bi.getStatus());
        dto.setCreatedAt(bi.getCreatedAt());
        return dto;
    }
}
