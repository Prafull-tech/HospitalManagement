package com.hospital.hms.billing.service;

import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.billing.dto.BillingItemResponseDto;
import com.hospital.hms.billing.dto.PaymentRequestDto;
import com.hospital.hms.billing.entity.BillingItem;
import com.hospital.hms.billing.entity.PatientBillingAccount;
import com.hospital.hms.billing.entity.Payment;
import com.hospital.hms.billing.entity.BillingServiceType;
import com.hospital.hms.billing.repository.BillingItemRepository;
import com.hospital.hms.billing.repository.EMIPlanRepository;
import com.hospital.hms.billing.repository.PatientBillingAccountRepository;
import com.hospital.hms.billing.repository.PaymentRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.reception.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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

    private static final Logger log = LoggerFactory.getLogger(BillingAccountService.class);

    private final PatientBillingAccountRepository accountRepository;
    private final BillingItemRepository itemRepository;
    private final PaymentRepository paymentRepository;
    private final EMIPlanRepository emiPlanRepository;
    private final IPDAdmissionRepository admissionRepository;
    private final PatientRepository patientRepository;

    public BillingAccountService(PatientBillingAccountRepository accountRepository,
                                 BillingItemRepository itemRepository,
                                 PaymentRepository paymentRepository,
                                 EMIPlanRepository emiPlanRepository,
                                 IPDAdmissionRepository admissionRepository,
                                 PatientRepository patientRepository) {
        this.accountRepository = accountRepository;
        this.itemRepository = itemRepository;
        this.paymentRepository = paymentRepository;
        this.emiPlanRepository = emiPlanRepository;
        this.admissionRepository = admissionRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public BillingAccountViewDto getAccountViewByBillingAccountId(Long billingAccountId) {
        PatientBillingAccount account = accountRepository.findById(billingAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing account not found: " + billingAccountId));
        var patient = patientRepository.findById(account.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + account.getPatientId()));
        List<BillingItem> items = itemRepository.findPostedByBillingAccountId(account.getId());
        Map<BillingServiceType, BigDecimal> byType = new HashMap<>();
        for (BillingItem bi : items) {
            byType.merge(bi.getServiceType(), bi.getTotalPrice(), BigDecimal::add);
        }
        BillingAccountViewDto dto = new BillingAccountViewDto();
        dto.setId(account.getId());
        dto.setPatientId(account.getPatientId());
        dto.setUhid(account.getUhid());
        dto.setPatientName(patient.getFullName());
        dto.setIpdAdmissionId(account.getIpdAdmissionId());
        dto.setOpdVisitId(account.getOpdVisitId());
        dto.setBillStatus(account.getBillStatus());
        dto.setTotalAmount(account.getTotalAmount());
        dto.setPaidAmount(account.getPaidAmount());
        dto.setPendingAmount(account.getPendingAmount());
        dto.setInsuranceType(account.getInsuranceType());
        dto.setTpaApprovalStatus(account.getTpaApprovalStatus());
        dto.setTotalByServiceType(byType);
        dto.setItems(items.stream().map(this::toItemDto).collect(Collectors.toList()));
        dto.setCorporate(Boolean.TRUE.equals(account.getCorporate()));
        dto.setCorporateApproved(Boolean.TRUE.equals(account.getCorporateApproved()));
        dto.setEmiActive(emiPlanRepository.existsByBillingAccountIdAndStatus(account.getId(), com.hospital.hms.billing.entity.EMIPlan.EMIPlanStatus.ACTIVE));
        dto.setHasGstSplit(items.stream().anyMatch(bi -> bi.getCgst() != null && bi.getCgst().compareTo(BigDecimal.ZERO) > 0));
        return dto;
    }

    @Transactional(readOnly = true)
    public List<BillingItemResponseDto> getItemsByIpdAdmissionId(Long ipdAdmissionId) {
        PatientBillingAccount account = accountRepository.findByIpdAdmissionId(ipdAdmissionId)
                .orElseGet(() -> createAccountForIpd(ipdAdmissionId));
        List<BillingItem> items = itemRepository.findPostedByBillingAccountId(account.getId());
        return items.stream().map(this::toItemDto).collect(Collectors.toList());
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
        dto.setCorporate(Boolean.TRUE.equals(account.getCorporate()));
        dto.setCorporateApproved(Boolean.TRUE.equals(account.getCorporateApproved()));
        dto.setEmiActive(emiPlanRepository.existsByBillingAccountIdAndStatus(account.getId(), com.hospital.hms.billing.entity.EMIPlan.EMIPlanStatus.ACTIVE));
        dto.setHasGstSplit(items.stream().anyMatch(bi -> bi.getCgst() != null && bi.getCgst().compareTo(BigDecimal.ZERO) > 0));
        return dto;
    }

    @Transactional
    public BillingAccountViewDto recordPayment(Long ipdAdmissionId, BigDecimal amount) {
        return recordPaymentInternal(ipdAdmissionId, amount, null, null);
    }

    @Transactional
    public BillingAccountViewDto recordPayment(PaymentRequestDto request) {
        return recordPaymentInternal(
                request.getIpdId(),
                request.getAmount(),
                request.getMode(),
                request.getReferenceNo());
    }

    private BillingAccountViewDto recordPaymentInternal(Long ipdAdmissionId, BigDecimal amount,
                                                        String mode, String referenceNo) {
        PatientBillingAccount account = accountRepository.findByIpdAdmissionId(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing account not found for IPD: " + ipdAdmissionId));

        account.setPaidAmount(account.getPaidAmount().add(amount));
        account.setPendingAmount(account.getPendingAmount().subtract(amount));
        if (account.getPendingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            account.setBillStatus(com.hospital.hms.billing.entity.BillStatus.CLOSED);
        }
        accountRepository.save(account);

        Payment payment = new Payment();
        payment.setIpdAdmissionId(ipdAdmissionId);
        payment.setAmount(amount);
        payment.setMode(mode != null ? mode : "Cash");
        payment.setReferenceNo(referenceNo);
        payment.setCreatedBy(SecurityContextUserResolver.resolveUserId());
        payment.setCorrelationId(MDC.get(MdcKeys.CORRELATION_ID));
        paymentRepository.save(payment);

        log.info("Payment recorded for IPD {} amount {} mode {} by {} correlationId {}",
                ipdAdmissionId, amount, payment.getMode(), payment.getCreatedBy(), payment.getCorrelationId());

        return getAccountViewByIpdAdmissionId(ipdAdmissionId);
    }

    private PatientBillingAccount createAccountForIpd(Long ipdAdmissionId) {
        var admission = admissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId));
        var patient = admission.getPatient();
        PatientBillingAccount acc = new PatientBillingAccount();
        acc.setPatientId(patient.getId());
        acc.setUhid(patient.getUhid());
        acc.setIpdAdmissionId(ipdAdmissionId);
        acc.setBillStatus(com.hospital.hms.billing.entity.BillStatus.ACTIVE);
        acc.setTotalAmount(BigDecimal.ZERO);
        acc.setPaidAmount(BigDecimal.ZERO);
        acc.setPendingAmount(BigDecimal.ZERO);
        acc.setInsuranceType(admission.getInsuranceTpa());
        if (patient.getCorporateId() != null) {
            acc.setCorporate(true);
            acc.setCorporateAccountId(patient.getCorporateId());
        }
        return accountRepository.save(acc);
    }

    @Transactional(readOnly = true)
    public boolean isPaid(Long ipdAdmissionId) {
        return accountRepository.findByIpdAdmissionId(ipdAdmissionId)
                .map(a -> a.getPendingAmount().compareTo(BigDecimal.ZERO) <= 0)
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public boolean isCorporateApproved(Long ipdAdmissionId) {
        return accountRepository.findByIpdAdmissionId(ipdAdmissionId)
                .map(a -> Boolean.TRUE.equals(a.getCorporateApproved()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveEMIPlan(Long ipdAdmissionId) {
        return accountRepository.findByIpdAdmissionId(ipdAdmissionId)
                .map(a -> emiPlanRepository.existsByBillingAccountIdAndStatus(a.getId(), com.hospital.hms.billing.entity.EMIPlan.EMIPlanStatus.ACTIVE))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canDischarge(Long ipdAdmissionId) {
        return isPaid(ipdAdmissionId) || isCorporateApproved(ipdAdmissionId) || hasActiveEMIPlan(ipdAdmissionId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getPendingAmount(Long ipdAdmissionId) {
        return accountRepository.findByIpdAdmissionId(ipdAdmissionId)
                .map(PatientBillingAccount::getPendingAmount)
                .orElse(BigDecimal.ZERO);
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
        dto.setGstPercent(bi.getGstPercent());
        dto.setCgst(bi.getCgst());
        dto.setSgst(bi.getSgst());
        dto.setIgst(bi.getIgst());
        return dto;
    }
}
