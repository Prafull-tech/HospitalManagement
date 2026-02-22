package com.hospital.hms.billing.service;

import com.hospital.hms.billing.dto.TpaPreauthRequestDto;
import com.hospital.hms.billing.dto.TpaPreauthResponseDto;
import com.hospital.hms.billing.entity.PatientBillingAccount;
import com.hospital.hms.billing.repository.PatientBillingAccountRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * TPA pre-authorization. Updates billing account with approval status.
 * Audit: route navigation, module access, preauth attempts.
 */
@Service
public class TpaService {

    private static final Logger log = LoggerFactory.getLogger(TpaService.class);

    private final PatientBillingAccountRepository accountRepository;

    public TpaService(PatientBillingAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public TpaPreauthResponseDto submitPreauth(TpaPreauthRequestDto request) {
        PatientBillingAccount account = accountRepository.findByIpdAdmissionId(request.getIpdAdmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Billing account not found for IPD: " + request.getIpdAdmissionId()));

        String approvalNumber = "TPA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        account.setTpaApprovalStatus("PENDING");
        accountRepository.save(account);

        String user = SecurityContextUserResolver.resolveUserId();
        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        log.info("TPA preauth submitted for IPD {} amount {} approval {} by {} correlationId {}",
                request.getIpdAdmissionId(), request.getEstimatedAmount(), approvalNumber, user, correlationId);

        TpaPreauthResponseDto dto = new TpaPreauthResponseDto();
        dto.setIpdAdmissionId(request.getIpdAdmissionId());
        dto.setApprovalNumber(approvalNumber);
        dto.setStatus("PENDING");
        dto.setMessage("Pre-auth submitted. Awaiting TPA response.");
        return dto;
    }
}
