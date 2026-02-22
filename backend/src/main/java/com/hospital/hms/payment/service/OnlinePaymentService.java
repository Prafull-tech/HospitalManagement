package com.hospital.hms.payment.service;

import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.billing.dto.PaymentRequestDto;
import com.hospital.hms.billing.service.BillingAccountService;
import com.hospital.hms.payment.dto.PaymentConfirmRequestDto;
import com.hospital.hms.payment.dto.PaymentOrderResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Online payment (Razorpay/Stripe). Stub implementation.
 * Wire Razorpay SDK when RAZORPAY_KEY_ID is set.
 */
@Service
public class OnlinePaymentService {

    private static final Logger log = LoggerFactory.getLogger(OnlinePaymentService.class);

    @Value("${payment.razorpay.key-id:}")
    private String razorpayKeyId;

    @Value("${payment.razorpay.key-secret:}")
    private String razorpayKeySecret;

    private final BillingAccountService billingAccountService;

    public OnlinePaymentService(BillingAccountService billingAccountService) {
        this.billingAccountService = billingAccountService;
    }

    @Transactional(readOnly = true)
    public PaymentOrderResponseDto createOrder(Long ipdId, BigDecimal amount) {
        long amountPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();
        String orderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);

        PaymentOrderResponseDto dto = new PaymentOrderResponseDto();
        dto.setOrderId(orderId);
        dto.setKeyId(razorpayKeyId != null && !razorpayKeyId.isBlank() ? razorpayKeyId : "rzp_test_placeholder");
        dto.setAmountPaise(amountPaise);
        dto.setCurrency("INR");

        log.info("Payment order created for IPD {} amount {} orderId {}", ipdId, amount, orderId);
        return dto;
    }

    @Transactional
    public BillingAccountViewDto confirmAndRecord(PaymentConfirmRequestDto request) {
        // In production: verify Razorpay signature, then record
        if (razorpayKeySecret != null && !razorpayKeySecret.isBlank() && request.getSignature() != null) {
            // RazorpayUtils.verifyPaymentSignature(orderId, paymentId, signature, keySecret);
        }

        PaymentRequestDto payReq = new PaymentRequestDto();
        payReq.setIpdId(request.getIpdId());
        payReq.setAmount(request.getAmount());
        payReq.setMode("UPI");
        payReq.setReferenceNo(request.getPaymentId() != null ? request.getPaymentId() : request.getOrderId());

        BillingAccountViewDto updated = billingAccountService.recordPayment(payReq);
        log.info("Online payment confirmed for IPD {} amount {} orderId {}", request.getIpdId(), request.getAmount(), request.getOrderId());
        return updated;
    }
}
