package com.hospital.hms.billing.service;

import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.billing.dto.RefundRequestDto;
import com.hospital.hms.billing.entity.Payment;
import com.hospital.hms.billing.entity.Refund;
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
    private final BillingAccountService billingAccountService;

    public RefundService(PaymentRepository paymentRepository,
                        RefundRepository refundRepository,
                        BillingAccountService billingAccountService) {
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
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

        String user = SecurityContextUserResolver.resolveUserId();
        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        log.info("Refund requested for payment {} amount {} by {} correlationId {}",
                request.getPaymentId(), request.getAmount(), user, correlationId);

        return billingAccountService.getAccountViewByIpdAdmissionId(payment.getIpdAdmissionId());
    }
}
