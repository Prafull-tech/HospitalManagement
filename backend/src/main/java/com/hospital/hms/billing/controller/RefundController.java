package com.hospital.hms.billing.controller;

import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.billing.dto.RefundRequestDto;
import com.hospital.hms.billing.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Refund API.
 * POST /api/billing/refund — process refund request.
 */
@RestController
@RequestMapping("/billing")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING')")
    public ResponseEntity<BillingAccountViewDto> refund(@Valid @RequestBody RefundRequestDto request) {
        BillingAccountViewDto result = refundService.processRefund(request);
        return ResponseEntity.ok(result);
    }
}
