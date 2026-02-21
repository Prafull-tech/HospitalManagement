package com.hospital.hms.pharmacy.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import com.hospital.hms.pharmacy.dto.PurchaseRequestDto;
import com.hospital.hms.pharmacy.dto.SellRequestDto;
import com.hospital.hms.pharmacy.dto.StockTransactionResponseDto;
import com.hospital.hms.pharmacy.entity.PharmacyInvoice;
import com.hospital.hms.pharmacy.entity.MedicineMaster;
import com.hospital.hms.pharmacy.entity.SaleType;
import com.hospital.hms.pharmacy.entity.StockTransaction;
import com.hospital.hms.pharmacy.entity.StockTransactionType;
import com.hospital.hms.reception.entity.Patient;
import com.hospital.hms.reception.repository.PatientRepository;
import com.hospital.hms.pharmacy.exception.InsufficientStockException;
import com.hospital.hms.pharmacy.repository.MedicineMasterRepository;
import com.hospital.hms.pharmacy.repository.StockTransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockTransactionService {

    private static final Logger log = LoggerFactory.getLogger(StockTransactionService.class);

    private final MedicineMasterRepository medicineRepository;
    private final StockTransactionRepository transactionRepository;
    private final PatientRepository patientRepository;
    private final PharmacyInvoiceService pharmacyInvoiceService;

    public StockTransactionService(MedicineMasterRepository medicineRepository,
                                   StockTransactionRepository transactionRepository,
                                   PatientRepository patientRepository,
                                   PharmacyInvoiceService pharmacyInvoiceService) {
        this.medicineRepository = medicineRepository;
        this.transactionRepository = transactionRepository;
        this.patientRepository = patientRepository;
        this.pharmacyInvoiceService = pharmacyInvoiceService;
    }

    @Transactional
    public StockTransactionResponseDto purchase(PurchaseRequestDto request, String performedBy) {
        MedicineMaster medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found: " + request.getMedicineId()));
        if (!Boolean.TRUE.equals(medicine.getActive())) {
            throw new IllegalArgumentException("Cannot purchase for inactive medicine");
        }

        StockTransaction txn = new StockTransaction();
        txn.setMedicine(medicine);
        txn.setTransactionType(StockTransactionType.PURCHASE);
        txn.setQuantity(request.getQuantity());
        txn.setTransactionDate(request.getTransactionDate());
        txn.setBatchNumber(request.getBatchNumber() != null ? request.getBatchNumber().trim() : null);
        txn.setExpiryDate(request.getExpiryDate());
        txn.setSupplier(request.getSupplier() != null ? request.getSupplier().trim() : null);
        txn.setCostPerUnit(request.getCostPerUnit());
        txn.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
        txn.setPerformedBy(performedBy);
        txn.setPerformedAt(Instant.now());
        txn = transactionRepository.save(txn);

        int newQty = (medicine.getQuantity() != null ? medicine.getQuantity() : 0) + request.getQuantity();
        medicine.setQuantity(newQty);
        medicineRepository.save(medicine);

        try {
            MDC.put(MdcKeys.MODULE, "PHARMACY");
            log.info("PHARMACY_AUDIT stock_purchase medicineId={} medicineCode={} qty={} performedBy={} correlationId={}",
                    medicine.getId(), medicine.getMedicineCode(), request.getQuantity(), performedBy, MDC.get(MdcKeys.CORRELATION_ID));
        } finally {
            MDC.remove(MdcKeys.MODULE);
        }
        return toDto(txn);
    }

    @Transactional
    public com.hospital.hms.pharmacy.dto.PharmacySellResponseDto sell(SellRequestDto request, String performedBy) {
        java.util.List<com.hospital.hms.pharmacy.dto.SellLineItemDto> items = request.resolveLineItems();
        if (items.isEmpty()) {
            throw new IllegalArgumentException("At least one medicine line item is required.");
        }

        SaleType saleType = request.getSaleType() != null ? request.getSaleType() : SaleType.PATIENT;
        Patient patient = null;
        if (saleType == SaleType.PATIENT) {
            if (request.getPatientId() == null) {
                throw new IllegalArgumentException("Patient-linked sale requires patient selection.");
            }
            patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.getPatientId()));
            if (request.getManualPatientName() != null || request.getManualPhone() != null) {
                throw new IllegalArgumentException("Patient-linked sale cannot have manual patient fields.");
            }
        }
        if (saleType == SaleType.MANUAL) {
            if (request.getManualPatientName() == null || request.getManualPatientName().trim().isEmpty()) {
                throw new IllegalArgumentException("Manual sale requires patient name.");
            }
        }

        java.util.List<StockTransaction> txns = new java.util.ArrayList<>();
        for (com.hospital.hms.pharmacy.dto.SellLineItemDto item : items) {
            MedicineMaster medicine = medicineRepository.findById(item.getMedicineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine not found: " + item.getMedicineId()));
            if (!Boolean.TRUE.equals(medicine.getActive())) {
                throw new IllegalArgumentException("Cannot sell inactive medicine: " + medicine.getMedicineCode());
            }
            int currentQty = medicine.getQuantity() != null ? medicine.getQuantity() : 0;
            if (currentQty < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for " + medicine.getMedicineName() + ". Available: " + currentQty + ", requested: " + item.getQuantity());
            }

            StockTransaction txn = new StockTransaction();
            txn.setMedicine(medicine);
            txn.setTransactionType(StockTransactionType.SELL);
            txn.setQuantity(item.getQuantity());
            txn.setTransactionDate(request.getTransactionDate());
            txn.setSaleType(saleType);
            txn.setPatient(patient);
            txn.setManualPatientName(request.getManualPatientName() != null ? request.getManualPatientName().trim() : null);
            txn.setManualPhone(request.getManualPhone() != null ? request.getManualPhone().trim() : null);
            txn.setManualEmail(request.getManualEmail() != null ? request.getManualEmail().trim() : null);
            txn.setManualAddress(request.getManualAddress() != null ? request.getManualAddress().trim() : null);
            txn.setReference(request.getReference() != null ? request.getReference().trim() : null);
            txn.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
            txn.setPerformedBy(performedBy);
            txn.setPerformedAt(Instant.now());
            txn = transactionRepository.save(txn);
            txns.add(txn);

            int newQty = currentQty - item.getQuantity();
            medicine.setQuantity(newQty);
            medicineRepository.save(medicine);

            try {
                MDC.put(MdcKeys.MODULE, "PHARMACY");
                String patientInfo = saleType == SaleType.PATIENT && request.getPatientId() != null
                        ? "patientId=" + request.getPatientId()
                        : "manual=" + (request.getManualPatientName() != null ? request.getManualPatientName() : "");
                log.info("PHARMACY_AUDIT stock_sell medicineId={} medicineCode={} qty={} saleType={} {} performedBy={} correlationId={}",
                        medicine.getId(), medicine.getMedicineCode(), item.getQuantity(), saleType, patientInfo, performedBy, MDC.get(MdcKeys.CORRELATION_ID));
            } finally {
                MDC.remove(MdcKeys.MODULE);
            }
        }

        // Generate invoice PDF (does not rollback sale on failure)
        java.util.Optional<PharmacyInvoice> invOpt = pharmacyInvoiceService.generateAfterSale(txns, performedBy);
        com.hospital.hms.pharmacy.dto.PharmacySellResponseDto response = new com.hospital.hms.pharmacy.dto.PharmacySellResponseDto();
        response.setTransaction(toDto(txns.get(0)));
        invOpt.ifPresent(inv -> {
            response.setInvoiceNumber(inv.getInvoiceNumber());
            response.setPdfUrl("/pharmacy/invoice/" + inv.getInvoiceNumber());
        });
        return response;
    }

    @Transactional(readOnly = true)
    public List<StockTransactionResponseDto> listRecent(int limit) {
        return transactionRepository.findByTransactionDateBetweenOrderByPerformedAtDesc(
                        java.time.LocalDate.now().minusDays(30),
                        java.time.LocalDate.now(),
                        PageRequest.of(0, limit))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockTransactionResponseDto> listByMedicine(Long medicineId, int limit) {
        return transactionRepository.findByMedicine_IdOrderByPerformedAtDesc(medicineId, PageRequest.of(0, limit))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private StockTransactionResponseDto toDto(StockTransaction t) {
        StockTransactionResponseDto dto = new StockTransactionResponseDto();
        dto.setId(t.getId());
        dto.setMedicineId(t.getMedicine().getId());
        dto.setMedicineCode(t.getMedicine().getMedicineCode());
        dto.setMedicineName(t.getMedicine().getMedicineName());
        dto.setTransactionType(t.getTransactionType());
        dto.setQuantity(t.getQuantity());
        dto.setTransactionDate(t.getTransactionDate());
        dto.setBatchNumber(t.getBatchNumber());
        dto.setExpiryDate(t.getExpiryDate());
        dto.setSupplier(t.getSupplier());
        dto.setReference(t.getReference());
        dto.setSaleType(t.getSaleType());
        dto.setPatientId(t.getPatient() != null ? t.getPatient().getId() : null);
        dto.setManualPatientName(t.getManualPatientName());
        dto.setManualPhone(t.getManualPhone());
        dto.setManualEmail(t.getManualEmail());
        dto.setManualAddress(t.getManualAddress());
        dto.setCostPerUnit(t.getCostPerUnit());
        dto.setNotes(t.getNotes());
        dto.setPerformedBy(t.getPerformedBy());
        dto.setPerformedAt(t.getPerformedAt());
        return dto;
    }

    public static String resolvePerformedBy() {
        return SecurityContextUserResolver.resolveUserId();
    }
}
