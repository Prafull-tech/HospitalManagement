package com.hospital.hms.billing.service;

import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.billing.dto.RefundRequestDto;
import com.hospital.hms.billing.entity.PatientBillingAccount;
import com.hospital.hms.billing.entity.Payment;
import com.hospital.hms.billing.entity.Refund;
import com.hospital.hms.billing.repository.PatientBillingAccountRepository;
import com.hospital.hms.billing.repository.PaymentRepository;
import com.hospital.hms.billing.repository.RefundRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Refund processing. Creates refund record for audit.
 * Audit: refund requests logged.
 */
@Service
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PatientBillingAccountRepository accountRepository;
    private final BillingAccountService billingAccountService;

    public RefundService(PaymentRepository paymentRepository,
                        RefundRepository refundRepository,
                        PatientBillingAccountRepository accountRepository,
                        BillingAccountService billingAccountService) {
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.accountRepository = accountRepository;
        this.billingAccountService = billingAccountService;
    }

    @Transactional
    public BillingAccountViewDto processRefund(RefundRequestDto request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + request.getPaymentId()));

        Refund refund = new Refund();
        refund.setPaymentId(request.getPaymentId());
        refund.setAmount(request.getAmount());
        refund.setReason(request.getReason());
        refund.setCreatedBy(SecurityContextUserResolver.resolveUserId());
        refund.setCorrelationId(MDC.get(MdcKeys.CORRELATION_ID));
        refundRepository.save(refund);

        if (payment.getBillingAccountId() != null) {
            PatientBillingAccount account = accountRepository.findById(payment.getBillingAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Billing account not found: " + payment.getBillingAccountId()));
            account.setPaidAmount(account.getPaidAmount().subtract(refund.getAmount()));
            account.setPendingAmount(account.getPendingAmount().add(refund.getAmount()));
            accountRepository.save(account);
            log.info("Refund processed: accountId={}, amount={}, new paidAmount={}, new pendingAmount={}",
                    account.getId(), refund.getAmount(), account.getPaidAmount(), account.getPendingAmount());
        }

        String user = SecurityContextUserResolver.resolveUserId();
        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        log.info("Refund requested for payment {} amount {} by {} correlationId {}",
                request.getPaymentId(), request.getAmount(), user, correlationId);

        if (payment.getBillingAccountId() != null) {
            return billingAccountService.getAccountViewByBillingAccountId(payment.getBillingAccountId());
        }
        if (payment.getIpdAdmissionId() != null) {
            return billingAccountService.getAccountViewByIpdAdmissionId(payment.getIpdAdmissionId());
        }
        throw new ResourceNotFoundException("Payment has no linked encounter");
    }
}
