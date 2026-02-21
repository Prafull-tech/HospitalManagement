package com.hospital.hms.pharmacy.service;

import com.hospital.hms.billing.dto.AdmissionChargeRequestDto;
import com.hospital.hms.billing.entity.ChargeType;
import com.hospital.hms.billing.service.AdmissionChargeService;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.ipd.dto.DischargePendingItemDto;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.BedAllocationRepository;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.opd.entity.OPDVisit;
import com.hospital.hms.opd.repository.OPDVisitRepository;
import com.hospital.hms.pharmacy.dto.*;
import com.hospital.hms.pharmacy.entity.*;
import com.hospital.hms.pharmacy.exception.InsufficientStockException;
import com.hospital.hms.pharmacy.repository.*;
import com.hospital.hms.reception.entity.Patient;
import com.hospital.hms.reception.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Medication order service: issue queue, FEFO suggestion, issue action.
 * NABH / Medication Safety compliant.
 */
@Service
public class MedicationOrderService {

    private static final Logger log = LoggerFactory.getLogger(MedicationOrderService.class);
    private static final DateTimeFormatter EXPIRY_FMT = DateTimeFormatter.ofPattern("MM/yyyy");

    private final MedicationOrderRepository orderRepository;
    private final MedicineMasterRepository medicineRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final MedicationIssueAuditLogRepository auditLogRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final IPDAdmissionRepository admissionRepository;
    private final BedAllocationRepository bedAllocationRepository;
    private final OPDVisitRepository opdVisitRepository;
    private final AdmissionChargeService admissionChargeService;

    public MedicationOrderService(MedicationOrderRepository orderRepository,
                                 MedicineMasterRepository medicineRepository,
                                 StockTransactionRepository stockTransactionRepository,
                                 MedicationIssueAuditLogRepository auditLogRepository,
                                 PatientRepository patientRepository,
                                 DoctorRepository doctorRepository,
                                 IPDAdmissionRepository admissionRepository,
                                 BedAllocationRepository bedAllocationRepository,
                                 OPDVisitRepository opdVisitRepository,
                                 AdmissionChargeService admissionChargeService) {
        this.orderRepository = orderRepository;
        this.medicineRepository = medicineRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.auditLogRepository = auditLogRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.admissionRepository = admissionRepository;
        this.bedAllocationRepository = bedAllocationRepository;
        this.opdVisitRepository = opdVisitRepository;
        this.admissionChargeService = admissionChargeService;
    }

    @Transactional
    public MedicationOrder createOrder(MedicationOrderRequestDto request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.getPatientId()));
        MedicineMaster medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found: " + request.getMedicineId()));
        Doctor doctor = doctorRepository.findById(request.getOrderedByDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getOrderedByDoctorId()));

        MedicationOrder order = new MedicationOrder();
        order.setPatient(patient);
        order.setUhid(patient.getUhid());
        order.setIpdAdmissionId(request.getIpdAdmissionId());
        order.setOpdVisitId(request.getOpdVisitId());
        order.setWardType(parseWardType(request.getWardType()));
        order.setMedicine(medicine);
        order.setDosage(request.getDosage());
        order.setFrequency(request.getFrequency());
        order.setRoute(request.getRoute());
        order.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
        order.setPriority(parsePriority(request.getPriority(), request.getWardType()));
        order.setStatus(MedicationOrderStatus.PENDING);
        order.setOrderedByDoctor(doctor);
        order.setOrderedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<DischargePendingItemDto> getPendingByIpdAdmissionId(Long ipdAdmissionId) {
        return orderRepository.findByIpdAdmissionIdAndStatus(ipdAdmissionId, MedicationOrderStatus.PENDING).stream()
                .map(o -> new DischargePendingItemDto(
                        o.getId(),
                        o.getMedicine().getMedicineName() + " " + (o.getDosage() != null ? o.getDosage() : "") + " x" + o.getQuantity(),
                        o.getStatus().name()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IssueQueuePatientDto> getIssueQueue(String q) {
        List<MedicationOrder> orders = orderRepository.findByStatusOrderByPriorityAndOrderedAt(MedicationOrderStatus.PENDING);
        if (orders.isEmpty()) return Collections.emptyList();

        Map<String, IssueQueuePatientDto> byPatient = new LinkedHashMap<>();
        for (MedicationOrder o : orders) {
            String key = patientKey(o);
            if (q != null && !q.isBlank()) {
                String lower = q.toLowerCase();
                if (!matchesSearch(o, lower)) continue;
            }

            IssueQueuePatientDto dto = byPatient.computeIfAbsent(key, k -> buildPatientDto(o));
            IssueQueueMedicineDto medDto = new IssueQueueMedicineDto();
            medDto.setOrderId(o.getId());
            medDto.setMedicineId(o.getMedicine().getId());
            medDto.setMedicineName(o.getMedicine().getMedicineName());
            medDto.setQuantity(o.getQuantity());
            medDto.setDosage(o.getDosage());
            medDto.setRoute(o.getRoute());
            medDto.setLasa(Boolean.TRUE.equals(o.getMedicine().getLasaFlag()));
            medDto.setFefoSuggestion(getBatchSuggestion(o.getMedicine().getId()));
            dto.getMedicines().add(medDto);
            dto.getOrderIds().add(o.getId());
        }

        return new ArrayList<>(byPatient.values());
    }

    @Transactional(readOnly = true)
    public BatchSuggestionDto getBatchSuggestion(Long medicineId) {
        MedicineMaster medicine = medicineRepository.findById(medicineId)
                .orElse(null);
        if (medicine == null) return null;

        List<StockTransaction> batches = stockTransactionRepository.findFefoBatchesForMedicine(
                medicineId, LocalDate.now(), PageRequest.of(0, 1));
        BatchSuggestionDto dto = new BatchSuggestionDto();
        dto.setAvailableQty(medicine.getQuantity() != null ? medicine.getQuantity() : 0);
        if (medicine.getRack() != null) {
            dto.setRackLocation(medicine.getRack().getRackCode());
        }
        if (!batches.isEmpty()) {
            StockTransaction first = batches.get(0);
            dto.setBatchNo(first.getBatchNumber());
            dto.setExpiryDate(first.getExpiryDate() != null ? first.getExpiryDate().format(EXPIRY_FMT) : null);
        }
        return dto;
    }

    @Transactional
    public void issueOrders(MedicationIssueRequestDto request, String performedBy) {
        List<Long> orderIds = request.getOrderIds();
        if (orderIds == null || orderIds.isEmpty()) {
            throw new IllegalArgumentException("At least one order ID is required.");
        }

        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        List<MedicationOrder> orders = orderRepository.findAllById(orderIds);
        if (orders.size() != orderIds.size()) {
            throw new ResourceNotFoundException("One or more orders not found.");
        }

        for (MedicationOrder order : orders) {
            if (order.getStatus() != MedicationOrderStatus.PENDING) {
                throw new IllegalArgumentException("Order " + order.getId() + " is not PENDING.");
            }
            MedicineMaster medicine = order.getMedicine();
            int available = medicine.getQuantity() != null ? medicine.getQuantity() : 0;
            if (available < order.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for " + medicine.getMedicineName() + ". Available: " + available + ", requested: " + order.getQuantity());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        for (MedicationOrder order : orders) {
            dispenseAndCharge(order, performedBy, now, correlationId);
        }

        try {
            MDC.put(MdcKeys.MODULE, "PHARMACY");
            log.info("PHARMACY_AUDIT medication_issue orderIds={} performedBy={} correlationId={}",
                    orderIds, performedBy, correlationId);
        } finally {
            MDC.remove(MdcKeys.MODULE);
        }
    }

    private void dispenseAndCharge(MedicationOrder order, String performedBy, LocalDateTime now, String correlationId) {
        MedicineMaster medicine = order.getMedicine();
        int qty = order.getQuantity();

        StockTransaction txn = new StockTransaction();
        txn.setMedicine(medicine);
        txn.setTransactionType(StockTransactionType.SELL);
        txn.setQuantity(qty);
        txn.setTransactionDate(LocalDate.now());
        txn.setSaleType(SaleType.PATIENT);
        txn.setPatient(order.getPatient());
        txn.setReference("MED_ORDER_" + order.getId());
        txn.setBatchNumber(order.getBatchNumber());
        txn.setPerformedBy(performedBy);
        txn.setPerformedAt(java.time.Instant.now());
        txn = stockTransactionRepository.save(txn);

        int newQty = (medicine.getQuantity() != null ? medicine.getQuantity() : 0) - qty;
        medicine.setQuantity(newQty);
        medicineRepository.save(medicine);

        order.setStatus(MedicationOrderStatus.ISSUED);
        order.setIssuedAt(now);
        order.setIssuedBy(performedBy);
        order.setBatchNumber(txn.getBatchNumber());
        orderRepository.save(order);

        MedicationIssueAuditLog audit = new MedicationIssueAuditLog();
        audit.setMedicationOrderId(order.getId());
        audit.setPatientId(order.getPatient().getId());
        audit.setMedicineId(medicine.getId());
        audit.setQuantity(qty);
        audit.setBatchNumber(txn.getBatchNumber());
        audit.setIssuedBy(performedBy);
        audit.setIssuedAt(java.time.Instant.now());
        audit.setCorrelationId(correlationId);
        auditLogRepository.save(audit);

        if (order.getIpdAdmissionId() != null) {
            BigDecimal unitPrice = medicine.getUnitPrice() != null ? medicine.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(qty));
            AdmissionChargeRequestDto chargeReq = new AdmissionChargeRequestDto();
            chargeReq.setChargeType(ChargeType.PHARMACY);
            chargeReq.setAmount(amount);
            chargeReq.setDescription(medicine.getMedicineName() + " x" + qty);
            chargeReq.setReferenceType("MEDICATION_ORDER");
            chargeReq.setReferenceId(order.getId());
            admissionChargeService.addCharge(order.getIpdAdmissionId(), chargeReq);
        }
    }

    private String patientKey(MedicationOrder o) {
        return o.getPatient().getId() + "|" + o.getIpdAdmissionId() + "|" + o.getOpdVisitId();
    }

    private boolean matchesSearch(MedicationOrder o, String lower) {
        if (o.getPatient().getFullName() != null && o.getPatient().getFullName().toLowerCase().contains(lower)) return true;
        if (o.getUhid() != null && o.getUhid().toLowerCase().contains(lower)) return true;
        if (o.getIpdAdmissionId() != null) {
            return admissionRepository.findById(o.getIpdAdmissionId())
                    .map(a -> a.getAdmissionNumber() != null && a.getAdmissionNumber().toLowerCase().contains(lower))
                    .orElse(false);
        }
        if (o.getOpdVisitId() != null) {
            return opdVisitRepository.findById(o.getOpdVisitId())
                    .map(v -> v.getVisitNumber() != null && v.getVisitNumber().toLowerCase().contains(lower))
                    .orElse(false);
        }
        return false;
    }

    private IssueQueuePatientDto buildPatientDto(MedicationOrder o) {
        IssueQueuePatientDto dto = new IssueQueuePatientDto();
        dto.setPatientName(o.getPatient().getFullName());
        dto.setUhid(o.getUhid());
        dto.setWardType(o.getWardType().name());
        dto.setPriority(o.getPriority().name());
        dto.setMedicines(new ArrayList<>());
        dto.setOrderIds(new ArrayList<>());

        if (o.getIpdAdmissionId() != null) {
            admissionRepository.findById(o.getIpdAdmissionId()).ifPresent(adm -> {
                dto.setIpdNo(adm.getAdmissionNumber());
                bedAllocationRepository.findActiveByAdmissionIdWithBedAndRoom(adm.getId()).ifPresent(alloc -> {
                    dto.setBed(alloc.getBed().getBedNumber());
                    if (alloc.getBed().getWard() != null) {
                        dto.setWardType(alloc.getBed().getWard().getWardType().name());
                    }
                });
            });
        }
        if (o.getOpdVisitId() != null) {
            opdVisitRepository.findById(o.getOpdVisitId())
                    .ifPresent(v -> dto.setOpdVisitNo(v.getVisitNumber()));
        }
        if (dto.getBed() == null) dto.setBed("—");
        return dto;
    }

    private MedicationOrderWardType parseWardType(String s) {
        if (s == null || s.isBlank()) return MedicationOrderWardType.GENERAL;
        try {
            return MedicationOrderWardType.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MedicationOrderWardType.GENERAL;
        }
    }

    private MedicationOrderPriority parsePriority(String priority, String wardType) {
        if (priority != null && !priority.isBlank()) {
            try {
                return MedicationOrderPriority.valueOf(priority.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        if (wardType != null && (wardType.equalsIgnoreCase("ICU") || wardType.equalsIgnoreCase("EMERGENCY"))) {
            return MedicationOrderPriority.HIGH;
        }
        return MedicationOrderPriority.NORMAL;
    }
}
