package com.hospital.hms.billing.service;

import com.hospital.hms.billing.dto.AddBillingItemRequestDto;
import com.hospital.hms.billing.dto.BillingDashboardSummaryDto;
import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.billing.dto.BillingItemResponseDto;
import com.hospital.hms.billing.dto.BillingTransactionDto;
import com.hospital.hms.billing.dto.PaymentRequestDto;
import com.hospital.hms.billing.entity.BillStatus;
import com.hospital.hms.billing.entity.BillingItem;
import com.hospital.hms.billing.entity.BillingServiceType;
import com.hospital.hms.billing.entity.PatientBillingAccount;
import com.hospital.hms.billing.entity.Payment;
import com.hospital.hms.billing.repository.BillingItemRepository;
import com.hospital.hms.billing.repository.EMIPlanRepository;
import com.hospital.hms.billing.repository.PatientBillingAccountRepository;
import com.hospital.hms.billing.repository.PaymentRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.opd.entity.OPDVisit;
import com.hospital.hms.opd.repository.OPDVisitRepository;
import com.hospital.hms.reception.repository.PatientRepository;
import com.hospital.hms.tenant.service.TenantContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final OPDVisitRepository opdVisitRepository;
    private final PatientRepository patientRepository;
    private final BillingEngine billingEngine;
    private final TenantContextService tenantContextService;

    public BillingAccountService(PatientBillingAccountRepository accountRepository,
                                 BillingItemRepository itemRepository,
                                 PaymentRepository paymentRepository,
                                 EMIPlanRepository emiPlanRepository,
                                 IPDAdmissionRepository admissionRepository,
                                 OPDVisitRepository opdVisitRepository,
                                 PatientRepository patientRepository,
                                 BillingEngine billingEngine,
                                 TenantContextService tenantContextService) {
        this.accountRepository = accountRepository;
        this.itemRepository = itemRepository;
        this.paymentRepository = paymentRepository;
        this.emiPlanRepository = emiPlanRepository;
        this.admissionRepository = admissionRepository;
        this.opdVisitRepository = opdVisitRepository;
        this.patientRepository = patientRepository;
        this.billingEngine = billingEngine;
        this.tenantContextService = tenantContextService;
    }

    /**
     * When an OPD visit moves to COMPLETED, post a consultation line once (idempotent by referenceId).
     */
    @Transactional
    public void postOpdConsultationFeeIfAbsent(Long opdVisitId, java.math.BigDecimal unitPrice, String doctorDisplayName) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        PatientBillingAccount account = accountRepository.findByOpdVisitId(opdVisitId)
                .orElseGet(() -> createAccountForOpd(opdVisitId));
        var consultationItems = itemRepository.findPostedByBillingAccountIdAndServiceType(
                account.getId(), BillingServiceType.CONSULTATION);
        boolean already = consultationItems.stream().anyMatch(i -> opdVisitId.equals(i.getReferenceId()));
        if (already) {
            return;
        }
        AddBillingItemRequestDto req = new AddBillingItemRequestDto();
        req.setOpdVisitId(opdVisitId);
        req.setServiceType(BillingServiceType.CONSULTATION);
        req.setServiceName("OPD Consultation" + (doctorDisplayName != null && !doctorDisplayName.isBlank() ? " - " + doctorDisplayName : ""));
        req.setReferenceId(opdVisitId);
        req.setQuantity(1);
        req.setUnitPrice(unitPrice);
        req.setDepartment("OPD");
        billingEngine.addItem(req);
    }

    @Transactional(readOnly = true)
    public BillingDashboardSummaryDto getDashboardSummary(LocalDate date) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        LocalDate d = date != null ? date : LocalDate.now();
        ZonedDateTime start = d.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime end = d.atTime(23, 59, 59, 999_999_999).atZone(ZoneId.systemDefault());
        Instant from = start.toInstant();
        Instant to = end.toInstant();
        BigDecimal today = paymentRepository.sumAmountByHospitalIdAndCreatedAtBetween(hospitalId, from, to);
        if (today == null) {
            today = BigDecimal.ZERO;
        }
        long cnt = paymentRepository.countByHospitalIdAndCreatedAtBetween(hospitalId, from, to);
        BigDecimal pending = accountRepository.sumPendingActiveAccountsByHospitalId(hospitalId);
        if (pending == null) {
            pending = BigDecimal.ZERO;
        }
        BillingDashboardSummaryDto dto = new BillingDashboardSummaryDto();
        dto.setDate(d);
        dto.setTodayCollection(today);
        dto.setPaymentCountToday(cnt);
        dto.setTotalPendingActiveAccounts(pending);
        return dto;
    }

    private void validatePaymentTargets(PaymentRequestDto r) {
        int n = (r.getIpdId() != null ? 1 : 0) + (r.getOpdVisitId() != null ? 1 : 0) + (r.getBillingAccountId() != null ? 1 : 0);
        if (n != 1) {
            throw new IllegalArgumentException("Provide exactly one of ipdId, opdVisitId, billingAccountId");
        }
    }

    private PatientBillingAccount resolveAccount(PaymentRequestDto r) {
        if (r.getBillingAccountId() != null) {
            return accountRepository.findById(r.getBillingAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Billing account not found: " + r.getBillingAccountId()));
        }
        if (r.getIpdId() != null) {
            return accountRepository.findByIpdAdmissionId(r.getIpdId()).orElseGet(() -> createAccountForIpd(r.getIpdId()));
        }
        return accountRepository.findByOpdVisitId(r.getOpdVisitId()).orElseGet(() -> createAccountForOpd(r.getOpdVisitId()));
    }

    @Transactional(readOnly = true)
    public BillingAccountViewDto getAccountViewByBillingAccountId(Long billingAccountId) {
        PatientBillingAccount account = accountRepository.findById(billingAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing account not found: " + billingAccountId));
        return buildViewForAccount(account);
    }

    @Transactional(readOnly = true)
    public Page<BillingTransactionDto> listTransactions(Instant from, Instant to, Pageable pageable) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Page<Payment> payments = paymentRepository.findByHospitalIdAndCreatedAtBetween(hospitalId, from, to, pageable);
        List<Payment> content = payments.getContent();
        if (content.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        Set<Long> accountIds = new HashSet<>();
        Set<Long> legacyIpdIds = new HashSet<>();
        for (Payment p : content) {
            if (p.getBillingAccountId() != null) {
                accountIds.add(p.getBillingAccountId());
            } else if (p.getIpdAdmissionId() != null) {
                legacyIpdIds.add(p.getIpdAdmissionId());
            }
        }
        Map<Long, PatientBillingAccount> byAccId = accountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(PatientBillingAccount::getId, a -> a));
        Map<Long, PatientBillingAccount> byIpd = new HashMap<>();
        for (Long ipd : legacyIpdIds) {
            accountRepository.findByIpdAdmissionId(ipd).ifPresent(a -> byIpd.put(ipd, a));
        }
        Set<Long> admissionIdsForNames = new HashSet<>();
        Set<Long> visitIdsForNames = new HashSet<>();
        for (Payment p : content) {
            PatientBillingAccount acc = resolveAccountForPayment(p, byAccId, byIpd);
            if (acc != null) {
                if (acc.getIpdAdmissionId() != null) {
                    admissionIdsForNames.add(acc.getIpdAdmissionId());
                }
                if (acc.getOpdVisitId() != null) {
                    visitIdsForNames.add(acc.getOpdVisitId());
                }
            } else if (p.getIpdAdmissionId() != null) {
                admissionIdsForNames.add(p.getIpdAdmissionId());
            }
        }
        Map<Long, IPDAdmission> admissionMap = admissionRepository.findAllById(admissionIdsForNames).stream()
                .collect(Collectors.toMap(IPDAdmission::getId, a -> a));
        Map<Long, OPDVisit> visitMap = new HashMap<>();
        for (Long vid : visitIdsForNames) {
            opdVisitRepository.findByIdWithAssociations(vid).ifPresent(v -> visitMap.put(vid, v));
        }

        List<BillingTransactionDto> dtos = content.stream().map(p -> {
            BillingTransactionDto dto = new BillingTransactionDto();
            dto.setId(p.getId());
            dto.setAmount(p.getAmount());
            dto.setMode(p.getMode());
            dto.setCreatedAt(p.getCreatedAt());
            dto.setBillingAccountId(p.getBillingAccountId());
            dto.setIpdAdmissionId(p.getIpdAdmissionId());

            PatientBillingAccount acc = resolveAccountForPayment(p, byAccId, byIpd);
            if (acc != null) {
                dto.setBillingAccountId(acc.getId());
                if (acc.getIpdAdmissionId() != null) {
                    dto.setIpdAdmissionId(acc.getIpdAdmissionId());
                    dto.setService("IPD Payment");
                    IPDAdmission adm = admissionMap.get(acc.getIpdAdmissionId());
                    if (adm != null) {
                        dto.setAdmissionNumber(adm.getAdmissionNumber());
                        dto.setPatientName(adm.getPatient() != null ? adm.getPatient().getFullName() : null);
                        dto.setPatientUhid(adm.getPatient() != null ? adm.getPatient().getUhid() : null);
                    }
                } else if (acc.getOpdVisitId() != null) {
                    dto.setOpdVisitId(acc.getOpdVisitId());
                    dto.setService("OPD Payment");
                    OPDVisit v = visitMap.get(acc.getOpdVisitId());
                    if (v != null) {
                        dto.setVisitNumber(v.getVisitNumber());
                        dto.setPatientName(v.getPatient() != null ? v.getPatient().getFullName() : null);
                        dto.setPatientUhid(v.getPatient() != null ? v.getPatient().getUhid() : null);
                    }
                }
            } else if (p.getIpdAdmissionId() != null) {
                dto.setService("IPD Payment");
                IPDAdmission adm = admissionMap.get(p.getIpdAdmissionId());
                if (adm != null) {
                    dto.setAdmissionNumber(adm.getAdmissionNumber());
                    dto.setPatientName(adm.getPatient() != null ? adm.getPatient().getFullName() : null);
                    dto.setPatientUhid(adm.getPatient() != null ? adm.getPatient().getUhid() : null);
                }
            }
            return dto;
        }).toList();
        return new PageImpl<>(dtos, payments.getPageable(), payments.getTotalElements());
    }

    private static PatientBillingAccount resolveAccountForPayment(Payment p,
                                                                  Map<Long, PatientBillingAccount> byAccId,
                                                                  Map<Long, PatientBillingAccount> byIpd) {
        if (p.getBillingAccountId() != null) {
            return byAccId.get(p.getBillingAccountId());
        }
        if (p.getIpdAdmissionId() != null) {
            return byIpd.get(p.getIpdAdmissionId());
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<BillingItemResponseDto> getItemsByIpdAdmissionId(Long ipdAdmissionId) {
        PatientBillingAccount account = accountRepository.findByIpdAdmissionId(ipdAdmissionId)
                .orElseGet(() -> createAccountForIpd(ipdAdmissionId));
        List<BillingItem> items = itemRepository.findPostedByBillingAccountId(account.getId());
        return items.stream().map(this::toItemDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BillingItemResponseDto> getItemsByOpdVisitId(Long opdVisitId) {
        PatientBillingAccount account = accountRepository.findByOpdVisitId(opdVisitId)
                .orElseGet(() -> createAccountForOpd(opdVisitId));
        List<BillingItem> items = itemRepository.findPostedByBillingAccountId(account.getId());
        return items.stream().map(this::toItemDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BillingAccountViewDto getAccountViewByIpdAdmissionId(Long ipdAdmissionId) {
        PatientBillingAccount account = accountRepository.findByIpdAdmissionId(ipdAdmissionId)
                .orElseGet(() -> createAccountForIpd(ipdAdmissionId));
        IPDAdmission admission = admissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId));
        return buildViewForIpdAccount(account, admission);
    }

    @Transactional(readOnly = true)
    public BillingAccountViewDto getAccountViewByOpdVisitId(Long opdVisitId) {
        PatientBillingAccount account = accountRepository.findByOpdVisitId(opdVisitId)
                .orElseGet(() -> createAccountForOpd(opdVisitId));
        OPDVisit visit = opdVisitRepository.findByIdWithAssociations(opdVisitId)
                .orElseThrow(() -> new ResourceNotFoundException("OPD visit not found: " + opdVisitId));
        return buildViewForOpdAccount(account, visit);
    }

    private BillingAccountViewDto buildViewForAccount(PatientBillingAccount account) {
        var patient = patientRepository.findById(account.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + account.getPatientId()));
        List<BillingItem> items = itemRepository.findPostedByBillingAccountId(account.getId());
        Map<BillingServiceType, BigDecimal> byType = sumByServiceType(items);

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

        if (account.getIpdAdmissionId() != null) {
            admissionRepository.findById(account.getIpdAdmissionId()).ifPresent(a -> dto.setAdmissionNumber(a.getAdmissionNumber()));
        }
        if (account.getOpdVisitId() != null) {
            opdVisitRepository.findById(account.getOpdVisitId()).ifPresent(v -> dto.setVisitNumber(v.getVisitNumber()));
        }
        return dto;
    }

    private BillingAccountViewDto buildViewForIpdAccount(PatientBillingAccount account, IPDAdmission admission) {
        List<BillingItem> items = itemRepository.findPostedByBillingAccountId(account.getId());
        Map<BillingServiceType, BigDecimal> byType = sumByServiceType(items);

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

    private BillingAccountViewDto buildViewForOpdAccount(PatientBillingAccount account, OPDVisit visit) {
        List<BillingItem> items = itemRepository.findPostedByBillingAccountId(account.getId());
        Map<BillingServiceType, BigDecimal> byType = sumByServiceType(items);

        BillingAccountViewDto dto = new BillingAccountViewDto();
        dto.setId(account.getId());
        dto.setPatientId(account.getPatientId());
        dto.setUhid(account.getUhid());
        dto.setPatientName(visit.getPatient().getFullName());
        dto.setIpdAdmissionId(account.getIpdAdmissionId());
        dto.setOpdVisitId(account.getOpdVisitId());
        dto.setVisitNumber(visit.getVisitNumber());
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

    private static Map<BillingServiceType, BigDecimal> sumByServiceType(List<BillingItem> items) {
        Map<BillingServiceType, BigDecimal> byType = new HashMap<>();
        for (BillingItem bi : items) {
            byType.merge(bi.getServiceType(), bi.getTotalPrice(), BigDecimal::add);
        }
        return byType;
    }

    @Transactional
    public BillingAccountViewDto recordPayment(Long ipdAdmissionId, BigDecimal amount) {
        PatientBillingAccount account = accountRepository.findByIpdAdmissionId(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing account not found for IPD: " + ipdAdmissionId));
        return recordPaymentOnAccount(account, amount, null, null);
    }

    @Transactional
    public BillingAccountViewDto recordPayment(PaymentRequestDto request) {
        validatePaymentTargets(request);
        PatientBillingAccount account = resolveAccount(request);
        return recordPaymentOnAccount(account, request.getAmount(), request.getMode(), request.getReferenceNo());
    }

    private BillingAccountViewDto recordPaymentOnAccount(PatientBillingAccount account, BigDecimal amount,
                                                         String mode, String referenceNo) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (account.getPendingAmount().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Payment exceeds pending balance");
        }

        account.setPaidAmount(account.getPaidAmount().add(amount));
        account.setPendingAmount(account.getPendingAmount().subtract(amount));
        if (account.getPendingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            account.setBillStatus(BillStatus.CLOSED);
        }
        accountRepository.save(account);

        Payment payment = new Payment();
        payment.setBillingAccountId(account.getId());
        payment.setIpdAdmissionId(account.getIpdAdmissionId());
        payment.setAmount(amount);
        payment.setMode(mode != null ? mode : "Cash");
        payment.setReferenceNo(referenceNo);
        payment.setCreatedBy(SecurityContextUserResolver.resolveUserId());
        payment.setCorrelationId(MDC.get(MdcKeys.CORRELATION_ID));
        paymentRepository.save(payment);

        log.info("Payment recorded billingAccount={} ipd={} opd={} amount {} mode {} by {}",
                account.getId(), account.getIpdAdmissionId(), account.getOpdVisitId(), amount, payment.getMode(), payment.getCreatedBy());

        return viewAfterPayment(account);
    }

    private BillingAccountViewDto viewAfterPayment(PatientBillingAccount account) {
        if (account.getIpdAdmissionId() != null) {
            return getAccountViewByIpdAdmissionId(account.getIpdAdmissionId());
        }
        if (account.getOpdVisitId() != null) {
            return getAccountViewByOpdVisitId(account.getOpdVisitId());
        }
        return getAccountViewByBillingAccountId(account.getId());
    }

    private PatientBillingAccount createAccountForIpd(Long ipdAdmissionId) {
        IPDAdmission admission = admissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId));
        var patient = admission.getPatient();
        PatientBillingAccount acc = new PatientBillingAccount();
        acc.setPatientId(patient.getId());
        acc.setUhid(patient.getUhid());
        acc.setIpdAdmissionId(ipdAdmissionId);
        acc.setBillStatus(BillStatus.ACTIVE);
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

    private PatientBillingAccount createAccountForOpd(Long opdVisitId) {
        OPDVisit visit = opdVisitRepository.findByIdWithAssociations(opdVisitId)
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
