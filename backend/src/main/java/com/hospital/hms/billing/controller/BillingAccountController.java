package com.hospital.hms.billing.controller;

import com.hospital.hms.billing.dto.AddBillingItemRequestDto;
import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.billing.dto.BillingItemResponseDto;
import com.hospital.hms.billing.entity.BillingItem;
import com.hospital.hms.billing.service.BillingAccountService;
import com.hospital.hms.billing.service.BillingEngine;
import com.hospital.hms.ipd.dto.DischargeStatusDto;
import com.hospital.hms.ipd.service.DischargeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST API for centralized billing.
 * POST /api/billing/add-item — event-based charge capture
 * GET /api/billing/account/{ipdId} — billing dashboard
 * POST /api/billing/finalize/{ipdId} — finalize and record payment
 */
@RestController
@RequestMapping("/billing")
public class BillingAccountController {

    private final BillingEngine billingEngine;
    private final BillingAccountService billingAccountService;
    private final DischargeService dischargeService;

    public BillingAccountController(BillingEngine billingEngine,
                                   BillingAccountService billingAccountService,
                                   DischargeService dischargeService) {
        this.billingEngine = billingEngine;
        this.billingAccountService = billingAccountService;
        this.dischargeService = dischargeService;
    }

    @PostMapping("/add-item")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING', 'PHARMACIST', 'IPD_PHARMACIST', 'LAB_TECHNICIAN', 'LAB_SUPERVISOR', 'DOCTOR', 'NURSE')")
    public ResponseEntity<BillingItemResponseDto> addItem(@Valid @RequestBody AddBillingItemRequestDto request) {
        BillingItem item = billingEngine.addItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toItemDto(item));
    }

    @GetMapping("/account/{ipdAdmissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING', 'DOCTOR', 'NURSE')")
    public ResponseEntity<BillingAccountViewDto> getAccount(@PathVariable Long ipdAdmissionId) {
        return ResponseEntity.ok(billingAccountService.getAccountViewByIpdAdmissionId(ipdAdmissionId));
    }

    @PostMapping("/finalize/{ipdAdmissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING')")
    public ResponseEntity<DischargeStatusDto> finalizeBill(@PathVariable Long ipdAdmissionId,
                                                          @RequestParam(required = false) BigDecimal paymentAmount) {
        if (paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            billingAccountService.recordPayment(ipdAdmissionId, paymentAmount);
        }
        DischargeStatusDto status = dischargeService.recordBillingClearance(ipdAdmissionId);
        return ResponseEntity.ok(status);
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
        return dto;
    }
}
