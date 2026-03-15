package com.hospital.hms.billing.controller;

import com.hospital.hms.billing.dto.AddBillingItemRequestDto;
import com.hospital.hms.billing.dto.BillingAccountViewDto;
import com.hospital.hms.billing.dto.BillingItemResponseDto;
import com.hospital.hms.billing.dto.BillingTransactionDto;
import com.hospital.hms.billing.dto.PaymentRequestDto;
import com.hospital.hms.billing.entity.BillingItem;
import com.hospital.hms.billing.service.BillingAccountService;
import com.hospital.hms.billing.service.BillingEngine;
import com.hospital.hms.ipd.dto.DischargeStatusDto;
import com.hospital.hms.ipd.service.DischargeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    /** Alias for GET /api/billing/account/{ipdId} — IPD billing view. */
    @GetMapping("/ipd/{ipdAdmissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING', 'DOCTOR', 'NURSE')")
    public ResponseEntity<BillingAccountViewDto> getIpdBilling(@PathVariable Long ipdAdmissionId) {
        return ResponseEntity.ok(billingAccountService.getAccountViewByIpdAdmissionId(ipdAdmissionId));
    }

    @GetMapping("/account/{ipdAdmissionId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING', 'DOCTOR', 'NURSE')")
    public ResponseEntity<java.util.List<BillingItemResponseDto>> getAccountItems(@PathVariable Long ipdAdmissionId) {
        return ResponseEntity.ok(billingAccountService.getItemsByIpdAdmissionId(ipdAdmissionId));
    }

    @GetMapping("/transactions")
    public ResponseEntity<org.springframework.data.domain.Page<BillingTransactionDto>> listTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (from == null) from = LocalDate.now();
        if (to == null) to = LocalDate.now();
        if (from.isAfter(to)) { LocalDate t = from; from = to; to = t; }
        Instant instantFrom = ZonedDateTime.of(from.atStartOfDay(), ZoneId.systemDefault()).toInstant();
        Instant instantTo = ZonedDateTime.of(to.atTime(23, 59, 59, 999_999_999), ZoneId.systemDefault()).toInstant();
        Pageable pageable = PageRequest.of(page, Math.min(size, 500));
        return ResponseEntity.ok(billingAccountService.listTransactions(instantFrom, instantTo, pageable));
    }

    @PostMapping("/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING')")
    public ResponseEntity<BillingAccountViewDto> recordPayment(@Valid @RequestBody PaymentRequestDto request) {
        BillingAccountViewDto updated = billingAccountService.recordPayment(request);
        return ResponseEntity.ok(updated);
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
        dto.setGstPercent(bi.getGstPercent());
        dto.setCgst(bi.getCgst());
        dto.setSgst(bi.getSgst());
        dto.setIgst(bi.getIgst());
        return dto;
    }
}
