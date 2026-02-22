package com.hospital.hms.payment.controller;

import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.billing.service.BillingAccountService;
import com.hospital.hms.payment.dto.PaymentConfirmRequestDto;
import com.hospital.hms.payment.dto.PaymentOrderResponseDto;
import com.hospital.hms.payment.service.OnlinePaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Online payment integration. Razorpay / Stripe.
 * POST /api/payment/create-order - create order, return orderId
 * POST /api/payment/confirm - verify and record payment
 */
@RestController
@RequestMapping("/payment")
public class OnlinePaymentController {

    private final OnlinePaymentService onlinePaymentService;
    private final BillingAccountService billingAccountService;

    public OnlinePaymentController(OnlinePaymentService onlinePaymentService,
                                   BillingAccountService billingAccountService) {
        this.onlinePaymentService = onlinePaymentService;
        this.billingAccountService = billingAccountService;
    }

    @PostMapping("/create-order")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING')")
    public ResponseEntity<PaymentOrderResponseDto> createOrder(
            @RequestParam Long ipdId,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(onlinePaymentService.createOrder(ipdId, amount));
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING')")
    public ResponseEntity<BillingAccountViewDto> confirmPayment(@Valid @RequestBody PaymentConfirmRequestDto request) {
        return ResponseEntity.ok(onlinePaymentService.confirmAndRecord(request));
    }
}
