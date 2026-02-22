package com.hospital.hms.billing.service;

import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.billing.dto.CorporateInvoiceRequestDto;
import com.hospital.hms.billing.entity.CorporateAccount;
import com.hospital.hms.billing.entity.PatientBillingAccount;
import com.hospital.hms.billing.repository.CorporateAccountRepository;
import com.hospital.hms.billing.repository.PatientBillingAccountRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Corporate billing. Approve account for corporate tie-up discharge.
 */
@Service
public class CorporateBillingService {

    private static final Logger log = LoggerFactory.getLogger(CorporateBillingService.class);

    private final PatientBillingAccountRepository accountRepository;
    private final CorporateAccountRepository corporateAccountRepository;
    private final BillingAccountService billingAccountService;
    private final IPDAdmissionRepository admissionRepository;

    public CorporateBillingService(PatientBillingAccountRepository accountRepository,
                                   CorporateAccountRepository corporateAccountRepository,
                                   BillingAccountService billingAccountService,
                                   IPDAdmissionRepository admissionRepository) {
        this.accountRepository = accountRepository;
        this.corporateAccountRepository = corporateAccountRepository;
        this.billingAccountService = billingAccountService;
        this.admissionRepository = admissionRepository;
    }

    @Transactional
    public BillingAccountViewDto createCorporateInvoice(CorporateInvoiceRequestDto request) {
        CorporateAccount corp = corporateAccountRepository.findById(request.getCorporateAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Corporate account not found: " + request.getCorporateAccountId()));

        PatientBillingAccount account = accountRepository.findByIpdAdmissionId(request.getIpdAdmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Billing account not found for IPD: " + request.getIpdAdmissionId()));

        account.setCorporate(true);
        account.setCorporateAccountId(corp.getId());
        account.setCorporateApproved(true);
        accountRepository.save(account);

        String user = SecurityContextUserResolver.resolveUserId();
        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        log.info("Corporate invoice created for IPD {} corporate {} by {} correlationId {}",
                request.getIpdAdmissionId(), corp.getCorporateCode(), user, correlationId);

        return billingAccountService.getAccountViewByIpdAdmissionId(request.getIpdAdmissionId());
    }
}
