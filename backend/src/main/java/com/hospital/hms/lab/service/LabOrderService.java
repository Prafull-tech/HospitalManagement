package com.hospital.hms.lab.service;

import com.hospital.hms.billing.dto.AddBillingItemRequestDto;
import com.hospital.hms.billing.entity.BillingServiceType;
import com.hospital.hms.billing.service.BillingEngine;
import com.hospital.hms.common.exception.OperationNotAllowedException;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.lab.dto.LabOrderRequestDto;
import com.hospital.hms.lab.dto.LabOrderItemResponseDto;
import com.hospital.hms.lab.dto.LabOrderResponseDto;
import com.hospital.hms.lab.entity.LabAuditEventType;
import com.hospital.hms.lab.entity.*;
import com.hospital.hms.lab.repository.LabOrderItemRepository;
import com.hospital.hms.lab.repository.LabOrderRepository;
import com.hospital.hms.lab.repository.TestMasterRepository;
import com.hospital.hms.lab.repository.TestOrderRepository;
import com.hospital.hms.opd.entity.OPDVisit;
import com.hospital.hms.opd.repository.OPDVisitRepository;
import com.hospital.hms.reception.entity.Patient;
import com.hospital.hms.reception.repository.PatientRepository;
import com.hospital.hms.tenant.service.TenantContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for LabOrder (header + items) creation and retrieval.
 */
@Service
public class LabOrderService {

    private final LabOrderRepository labOrderRepository;
    private final LabOrderItemRepository labOrderItemRepository;
    private final TestMasterRepository testMasterRepository;
    private final TestOrderRepository testOrderRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final IPDAdmissionRepository ipdAdmissionRepository;
    private final OPDVisitRepository opdVisitRepository;
    private final TestOrderService testOrderService;
    private final BillingEngine billingEngine;
    private final LabAuditService labAuditService;
    private final TenantContextService tenantContextService;

    public LabOrderService(
            LabOrderRepository labOrderRepository,
            LabOrderItemRepository labOrderItemRepository,
            TestMasterRepository testMasterRepository,
            TestOrderRepository testOrderRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            IPDAdmissionRepository ipdAdmissionRepository,
            OPDVisitRepository opdVisitRepository,
            TestOrderService testOrderService,
            BillingEngine billingEngine,
            LabAuditService labAuditService,
            TenantContextService tenantContextService) {
        this.labOrderRepository = labOrderRepository;
        this.labOrderItemRepository = labOrderItemRepository;
        this.testMasterRepository = testMasterRepository;
        this.testOrderRepository = testOrderRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.ipdAdmissionRepository = ipdAdmissionRepository;
        this.opdVisitRepository = opdVisitRepository;
        this.testOrderService = testOrderService;
        this.billingEngine = billingEngine;
        this.labAuditService = labAuditService;
        this.tenantContextService = tenantContextService;
    }

    @Transactional
    public LabOrderResponseDto createOrder(LabOrderRequestDto request) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        Doctor doctor = doctorRepository.findByIdAndHospitalId(request.getOrderedByDoctorId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getOrderedByDoctorId()));

        Patient patient;
        IPDAdmission ipdAdmission = null;
        OPDVisit opdVisit = null;

        if (request.getIpdAdmissionId() != null) {
            ipdAdmission = ipdAdmissionRepository.findById(request.getIpdAdmissionId())
                    .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + request.getIpdAdmissionId()));
            patient = ipdAdmission.getPatient();
        } else if (request.getOpdVisitId() != null) {
            opdVisit = opdVisitRepository.findById(request.getOpdVisitId())
                    .orElseThrow(() -> new ResourceNotFoundException("OPD visit not found: " + request.getOpdVisitId()));
            patient = opdVisit.getPatient();
        } else if (request.getPatientId() != null) {
            patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.getPatientId()));
        } else {
            throw new IllegalArgumentException("Either ipdAdmissionId, opdVisitId, or patientId must be provided");
        }

        validatePatientBelongsToCurrentHospital(patient);

        LabOrderPriority orderPriority = request.getPriority() != null ? request.getPriority() : LabOrderPriority.NORMAL;
        if (Boolean.TRUE.equals(request.getIsPriority())) {
            orderPriority = LabOrderPriority.EMERGENCY;
        }
        boolean isPriority = orderPriority == LabOrderPriority.EMERGENCY;

        LabOrder order = new LabOrder();
        order.setPatient(patient);
        order.setUhid(patient.getUhid());
        order.setIpdAdmission(ipdAdmission);
        order.setOpdVisit(opdVisit);
        order.setOrderedByDoctor(doctor);
        order.setPriority(orderPriority);
        order.setStatus(LabOrderStatus.ORDERED);
        order.setOrderedAt(LocalDateTime.now());

        order = labOrderRepository.save(order);

        List<Long> testIds = request.getTestIds();
        if (testIds == null || testIds.isEmpty()) {
            if (request.getTestMasterId() != null) {
                testIds = List.of(request.getTestMasterId());
            } else {
                throw new IllegalArgumentException("At least one test (testIds or testMasterId) is required");
            }
        }

        for (Long testId : testIds) {
            TestMaster testMaster = testMasterRepository.findById(testId)
                    .orElseThrow(() -> new ResourceNotFoundException("Test not found: " + testId));
            if (!Boolean.TRUE.equals(testMaster.getActive())) {
                throw new IllegalArgumentException("Test is inactive: " + testMaster.getTestCode());
            }

            TestOrder testOrder = testOrderService.createTestOrderEntity(
                    patient, doctor, ipdAdmission, opdVisit, testMaster, isPriority);

            LabOrderItem item = new LabOrderItem();
            item.setOrder(order);
            item.setTestMaster(testMaster);
            item.setStatus(LabOrderItemStatus.ORDERED);
            item.setSampleStatus(LabOrderItemSampleStatus.PENDING);
            item.setTestOrder(testOrder);
            labOrderItemRepository.save(item);
            order.getItems().add(item);

            // Billing integration: POST /api/billing/add-item, Service Type = LAB
            if (ipdAdmission != null || opdVisit != null) {
                AddBillingItemRequestDto billingReq = new AddBillingItemRequestDto();
                billingReq.setServiceType(BillingServiceType.LAB);
                billingReq.setServiceName(testMaster.getTestName());
                billingReq.setUnitPrice(testMaster.getPrice() != null ? testMaster.getPrice() : BigDecimal.ZERO);
                billingReq.setQuantity(1);
                billingReq.setReferenceId(item.getId());
                billingReq.setDepartment("LAB");
                billingReq.setChargeDate(LocalDate.now());
                if (ipdAdmission != null) {
                    billingReq.setIpdAdmissionId(ipdAdmission.getId());
                }
                if (opdVisit != null) {
                    billingReq.setOpdVisitId(opdVisit.getId());
                }
                billingEngine.addItem(billingReq);
            }
        }

        return toDto(order);
    }

    private void validatePatientBelongsToCurrentHospital(Patient patient) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        if (patient.getHospital() == null || !patient.getHospital().getId().equals(hospitalId)) {
            throw new OperationNotAllowedException("Patient does not belong to current hospital");
        }
    }

    @Transactional(readOnly = true)
    public List<LabOrderResponseDto> listOrders(Long ipdAdmissionId, Long opdVisitId, Long patientId) {
        if (ipdAdmissionId != null) {
            return labOrderRepository.findByIpdAdmission_IdOrderByOrderedAtDesc(ipdAdmissionId).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
        if (opdVisitId != null) {
            return labOrderRepository.findByOpdVisit_IdOrderByOrderedAtDesc(opdVisitId).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
        if (patientId != null) {
            return labOrderRepository.findByPatientIdOrderByOrderedAtDesc(patientId).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
        return labOrderRepository.findAllByOrderByOrderedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LabOrderResponseDto getOrder(Long id) {
        LabOrder order = labOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab order not found: " + id));
        return toDto(order);
    }

    @Transactional(readOnly = true)
    public LabOrderItemResponseDto getOrderItem(Long orderItemId) {
        LabOrderItem item = labOrderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab order item not found: " + orderItemId));
        return toPendingProcessingItemDto(item);
    }

    /** LabOrderItems whose sample is collected but not yet completed (TestOrder status COLLECTED or IN_PROGRESS). */
    @Transactional(readOnly = true)
    public List<LabOrderItemResponseDto> getPendingProcessingItems() {
        return labOrderItemRepository.findByTestOrderStatusInOrderByIdAsc(
                java.util.List.of(TestStatus.COLLECTED, TestStatus.IN_PROGRESS)).stream()
                .map(this::toPendingProcessingItemDto)
                .collect(Collectors.toList());
    }

    /** Process a lab order item: action START -> IN_PROGRESS, COMPLETE -> COMPLETED. */
    @Transactional
    public LabOrderItemResponseDto processItem(Long orderItemId, String action) {
        LabOrderItem item = labOrderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab order item not found: " + orderItemId));
        TestOrder testOrder = item.getTestOrder();
        if (testOrder == null) {
            throw new IllegalArgumentException("Lab order item has no linked test order");
        }
        if (testOrder.getStatus() != TestStatus.COLLECTED && testOrder.getStatus() != TestStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Only COLLECTED or IN_PROGRESS items can be processed. Current: " + testOrder.getStatus());
        }
        if ("START".equalsIgnoreCase(action)) {
            item.setStatus(LabOrderItemStatus.IN_PROGRESS);
            item.setSampleStatus(LabOrderItemSampleStatus.COLLECTED);
            testOrder.setStatus(TestStatus.IN_PROGRESS);
        } else if ("COMPLETE".equalsIgnoreCase(action)) {
            item.setStatus(LabOrderItemStatus.COMPLETED);
            item.setSampleStatus(LabOrderItemSampleStatus.COLLECTED);
            testOrder.setStatus(TestStatus.COMPLETED);
        } else {
            throw new IllegalArgumentException("Supported actions: START, COMPLETE");
        }
        testOrderRepository.save(testOrder);
        item = labOrderItemRepository.save(item);
        return toPendingProcessingItemDto(item);
    }

    /** LabOrderItems with results entered, awaiting verification (TestOrder status = COMPLETED). */
    @Transactional(readOnly = true)
    public List<LabOrderItemResponseDto> getPendingVerificationItems() {
        return labOrderItemRepository.findByTestOrderStatusOrderByIdAsc(TestStatus.COMPLETED).stream()
                .map(this::toPendingProcessingItemDto)
                .collect(Collectors.toList());
    }

    /** Verify or reject result: action VERIFY -> VERIFIED, REJECT -> REJECTED. Only senior technician/pathologist. */
    @Transactional
    public LabOrderItemResponseDto verifyResult(Long orderItemId, String action, String verifiedBy) {
        LabOrderItem item = labOrderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab order item not found: " + orderItemId));
        TestOrder testOrder = item.getTestOrder();
        if (testOrder == null) {
            throw new IllegalArgumentException("Lab order item has no linked test order");
        }
        if (testOrder.getStatus() != TestStatus.COMPLETED) {
            throw new IllegalArgumentException("Only COMPLETED results can be verified. Current: " + testOrder.getStatus());
        }
        if ("VERIFY".equalsIgnoreCase(action)) {
            item.setStatus(LabOrderItemStatus.VERIFIED);
            testOrder.setStatus(TestStatus.VERIFIED);
            LocalDateTime verifiedAt = LocalDateTime.now();
            testOrder.setVerifiedAt(verifiedAt);
            testOrder.setVerifiedBy(verifiedBy);
            testOrder.setTatEndTime(verifiedAt.atZone(java.time.ZoneId.systemDefault()).toInstant());
            evaluateTAT(testOrder);
            labAuditService.log(LabAuditEventType.RESULT_VERIFIED, testOrder.getId(), item.getId(), null, verifiedBy,
                    testOrder.getOrderNumber());
        } else if ("REJECT".equalsIgnoreCase(action)) {
            item.setStatus(LabOrderItemStatus.REJECTED);
            testOrder.setStatus(TestStatus.REJECTED);
        } else {
            throw new IllegalArgumentException("Supported actions: VERIFY, REJECT");
        }
        testOrderRepository.save(testOrder);
        item = labOrderItemRepository.save(item);
        return toPendingProcessingItemDto(item);
    }

    /** TAT = Result Verified Time - Sample Collection Time. Mark BREACH if exceeded. */
    private void evaluateTAT(TestOrder order) {
        Instant start = order.getTatStartTime();
        if (start == null && order.getSampleCollectedAt() != null) {
            start = order.getSampleCollectedAt().atZone(java.time.ZoneId.systemDefault()).toInstant();
        }
        Instant end = order.getTatEndTime();
        if (start == null || end == null || order.getTestMaster().getNormalTATMinutes() == null) return;
        long actualMinutes = Duration.between(start, end).toMinutes();
        int normalMinutes = order.getTestMaster().getNormalTATMinutes();
        if (actualMinutes > normalMinutes) {
            order.setTatStatus(com.hospital.hms.lab.entity.TATStatus.BREACH);
            if (order.getTatBreachReason() == null || order.getTatBreachReason().isEmpty()) {
                order.setTatBreachReason("TAT exceeded by " + (actualMinutes - normalMinutes) + " minutes");
            }
        } else {
            order.setTatStatus(com.hospital.hms.lab.entity.TATStatus.WITHIN_TAT);
        }
    }

    private LabOrderItemResponseDto toPendingProcessingItemDto(LabOrderItem item) {
        LabOrderItemResponseDto dto = new LabOrderItemResponseDto();
        dto.setId(item.getId());
        dto.setOrderId(item.getOrder().getId());
        dto.setTestId(item.getTestMaster().getId());
        dto.setTestCode(item.getTestMaster().getTestCode());
        dto.setTestName(item.getTestMaster().getTestName());
        dto.setStatus(item.getStatus());
        dto.setSampleStatus(item.getSampleStatus());
        if (item.getTestOrder() != null) {
            dto.setTestOrderId(item.getTestOrder().getId());
            dto.setOrderNumber(item.getTestOrder().getOrderNumber());
            var patient = item.getTestOrder().getPatient();
            dto.setPatientUhid(patient != null ? patient.getUhid() : null);
            dto.setPatientName(patient != null ? patient.getFullName() : null);
            dto.setSampleCollectedAt(item.getTestOrder().getSampleCollectedAt() != null
                    ? item.getTestOrder().getSampleCollectedAt().toString() : null);
            dto.setIsPriority(Boolean.TRUE.equals(item.getTestOrder().getIsPriority()));
            dto.setTestOrderStatus(item.getTestOrder().getStatus());
            if (item.getTestOrder().getResultEnteredAt() != null) {
                dto.setResultEnteredAt(item.getTestOrder().getResultEnteredAt().toString());
            }
            dto.setResultEnteredBy(item.getTestOrder().getResultEnteredBy());
        }
        return dto;
    }

    private LabOrderResponseDto toDto(LabOrder o) {
        LabOrderResponseDto dto = new LabOrderResponseDto();
        dto.setId(o.getId());
        dto.setPatientId(o.getPatient().getId());
        dto.setUhid(o.getUhid());
        dto.setPatientName(o.getPatient().getFullName());
        if (o.getIpdAdmission() != null) {
            dto.setIpdAdmissionId(o.getIpdAdmission().getId());
            dto.setIpdAdmissionNumber(o.getIpdAdmission().getAdmissionNumber());
        }
        if (o.getOpdVisit() != null) {
            dto.setOpdVisitId(o.getOpdVisit().getId());
            dto.setOpdVisitNumber(o.getOpdVisit().getVisitNumber());
        }
        dto.setOrderedByDoctorId(o.getOrderedByDoctor().getId());
        dto.setOrderedByDoctorName(o.getOrderedByDoctor().getFullName());
        dto.setPriority(o.getPriority());
        dto.setStatus(o.getStatus());
        dto.setOrderedAt(o.getOrderedAt());

        List<LabOrderItemResponseDto> items = new ArrayList<>();
        for (LabOrderItem item : o.getItems()) {
            LabOrderItemResponseDto itemDto = new LabOrderItemResponseDto();
            itemDto.setId(item.getId());
            itemDto.setOrderId(o.getId());
            itemDto.setTestId(item.getTestMaster().getId());
            itemDto.setTestCode(item.getTestMaster().getTestCode());
            itemDto.setTestName(item.getTestMaster().getTestName());
            itemDto.setStatus(item.getStatus());
            itemDto.setSampleStatus(item.getSampleStatus());
            if (item.getTestOrder() != null) {
                itemDto.setTestOrderId(item.getTestOrder().getId());
            }
            items.add(itemDto);
        }
        dto.setItems(items);
        return dto;
    }
}
