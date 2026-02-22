package com.hospital.hms.billing.service;

import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.billing.dto.EMICreateRequestDto;
import com.hospital.hms.billing.entity.EMIPlan;
import com.hospital.hms.billing.entity.PatientBillingAccount;
import com.hospital.hms.billing.repository.EMIPlanRepository;
import com.hospital.hms.billing.repository.PatientBillingAccountRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * EMI plan creation. Allows discharge when EMI active.
 */
@Service
public class EMIPlanService {

    private static final Logger log = LoggerFactory.getLogger(EMIPlanService.class);

    private final PatientBillingAccountRepository accountRepository;
    private final EMIPlanRepository emiPlanRepository;
    private final BillingAccountService billingAccountService;

    public EMIPlanService(PatientBillingAccountRepository accountRepository,
                          EMIPlanRepository emiPlanRepository,
                          BillingAccountService billingAccountService) {
        this.accountRepository = accountRepository;
        this.emiPlanRepository = emiPlanRepository;
        this.billingAccountService = billingAccountService;
    }

    @Transactional
    public BillingAccountViewDto createEMIPlan(EMICreateRequestDto request) {
        PatientBillingAccount account = accountRepository.findById(request.getBillingAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Billing account not found: " + request.getBillingAccountId()));

        BigDecimal remaining = request.getTotalAmount().subtract(request.getDownPayment());
        BigDecimal emiAmount = remaining.divide(BigDecimal.valueOf(request.getTenureMonths()), 2, RoundingMode.HALF_UP);

        EMIPlan plan = new EMIPlan();
        plan.setBillingAccountId(account.getId());
        plan.setIpdAdmissionId(request.getIpdAdmissionId());
        plan.setTotalAmount(request.getTotalAmount());
        plan.setDownPayment(request.getDownPayment());
        plan.setTenureMonths(request.getTenureMonths());
        plan.setEmiAmount(emiAmount);
        plan.setNextDueDate(LocalDate.now().plusMonths(1));
        plan.setStatus(EMIPlan.EMIPlanStatus.ACTIVE);
        emiPlanRepository.save(plan);

        account.setPaidAmount(account.getPaidAmount().add(request.getDownPayment()));
        account.setPendingAmount(account.getPendingAmount().subtract(request.getDownPayment()));
        accountRepository.save(account);

        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        log.info("EMI plan created for billing account {} tenure {} emi {} correlationId {}",
                account.getId(), request.getTenureMonths(), emiAmount, correlationId);

        Long ipdId = account.getIpdAdmissionId() != null ? account.getIpdAdmissionId() : request.getIpdAdmissionId();
        if (ipdId != null) {
            return billingAccountService.getAccountViewByIpdAdmissionId(ipdId);
        }
        throw new IllegalArgumentException("EMI plan requires IPD admission for discharge. Provide ipdAdmissionId.");
    }
}
