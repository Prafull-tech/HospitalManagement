package com.hospital.hms.lab.service;

import com.hospital.hms.billing.dto.AdmissionChargeRequestDto;
import com.hospital.hms.billing.entity.ChargeType;
import com.hospital.hms.billing.service.AdmissionChargeService;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.ipd.dto.DischargePendingItemDto;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.lab.dto.TestOrderRequestDto;
import com.hospital.hms.lab.dto.TestOrderResponseDto;
import com.hospital.hms.lab.entity.TestMaster;
import com.hospital.hms.lab.entity.TestOrder;
import com.hospital.hms.lab.entity.TestStatus;
import com.hospital.hms.lab.repository.TestMasterRepository;
import com.hospital.hms.lab.repository.TestOrderRepository;
import com.hospital.hms.opd.entity.OPDVisit;
import com.hospital.hms.opd.repository.OPDVisitRepository;
import com.hospital.hms.reception.entity.Patient;
import com.hospital.hms.reception.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for test ordering (OPD/IPD), panel expansion, billing integration, and doctor reference validation.
 */
@Service
public class TestOrderService {

    private final TestOrderRepository testOrderRepository;
    private final TestMasterRepository testMasterRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final IPDAdmissionRepository ipdAdmissionRepository;
    private final OPDVisitRepository opdVisitRepository;
    private final TestOrderNumberGenerator orderNumberGenerator;
    private final TestMasterService testMasterService;
    private final AdmissionChargeService admissionChargeService;

    public TestOrderService(
            TestOrderRepository testOrderRepository,
            TestMasterRepository testMasterRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            IPDAdmissionRepository ipdAdmissionRepository,
            OPDVisitRepository opdVisitRepository,
            TestOrderNumberGenerator orderNumberGenerator,
            TestMasterService testMasterService,
            AdmissionChargeService admissionChargeService) {
        this.testOrderRepository = testOrderRepository;
        this.testMasterRepository = testMasterRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.ipdAdmissionRepository = ipdAdmissionRepository;
        this.opdVisitRepository = opdVisitRepository;
        this.orderNumberGenerator = orderNumberGenerator;
        this.testMasterService = testMasterService;
        this.admissionChargeService = admissionChargeService;
    }

    /**
     * Create a test order (OPD or IPD). If test is a panel, expands into individual test orders.
     * For IPD: posts charge to billing immediately.
     * For OPD: billing handled separately (payment before report release).
     */
    @Transactional
    public List<TestOrderResponseDto> createOrder(TestOrderRequestDto request, String performedBy) {
        // Validate doctor
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getDoctorId()));

        // Validate test master
        TestMaster testMaster = testMasterRepository.findById(request.getTestMasterId())
                .orElseThrow(() -> new ResourceNotFoundException("Test master not found: " + request.getTestMasterId()));

        if (!Boolean.TRUE.equals(testMaster.getActive())) {
            throw new IllegalArgumentException("Test is inactive: " + testMaster.getTestCode());
        }

        // Determine patient and IPD/OPD context
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
        } else {
            throw new IllegalArgumentException("Either ipdAdmissionId or opdVisitId must be provided");
        }

        // Handle panel expansion
        List<TestMaster> testsToOrder = new ArrayList<>();
        if (Boolean.TRUE.equals(testMaster.getIsPanel())) {
            // Expand panel
            List<String> panelTestCodes = testMasterService.expandPanel(testMaster.getTestCode());
            for (String testCode : panelTestCodes) {
                TestMaster panelTest = testMasterRepository.findByTestCodeIgnoreCase(testCode)
                        .orElseThrow(() -> new ResourceNotFoundException("Panel test not found: " + testCode));
                if (Boolean.TRUE.equals(panelTest.getActive())) {
                    testsToOrder.add(panelTest);
                }
            }
        } else {
            testsToOrder.add(testMaster);
        }

        // Create orders for each test
        List<TestOrderResponseDto> createdOrders = new ArrayList<>();
        for (TestMaster tm : testsToOrder) {
            TestOrder order = new TestOrder();
            order.setOrderNumber(orderNumberGenerator.generate());
            order.setPatient(patient);
            order.setTestMaster(tm);
            order.setDoctor(doctor);
            order.setIpdAdmission(ipdAdmission);
            order.setOpdVisit(opdVisit);
            order.setStatus(TestStatus.ORDERED);
            order.setOrderedAt(LocalDateTime.now());
            order.setClinicalNotes(request.getClinicalNotes());
            order.setIsPriority(request.getIsPriority() != null ? request.getIsPriority() : Boolean.FALSE);

            order = testOrderRepository.save(order);

            // For IPD: post charge to billing immediately
            if (ipdAdmission != null && !order.getBillingChargePosted()) {
                AdmissionChargeRequestDto chargeRequest = new AdmissionChargeRequestDto();
                chargeRequest.setChargeType(ChargeType.LAB);
                chargeRequest.setAmount(tm.getPrice());
                chargeRequest.setDescription("Lab Test: " + tm.getTestName() + " (" + tm.getTestCode() + ")");
                chargeRequest.setReferenceType("LAB_TEST_ORDER");
                chargeRequest.setReferenceId(order.getId());

                try {
                    var charge = admissionChargeService.addCharge(ipdAdmission.getId(), chargeRequest);
                    order.setBillingChargePosted(true);
                    order.setBillingChargeId(charge.getId());
                    testOrderRepository.save(order);
                } catch (Exception e) {
                    // Log error but don't fail order creation
                    // Billing can be posted later
                }
            }

            createdOrders.add(toDto(order));
        }

        return createdOrders;
    }

    /**
     * Create a single TestOrder entity for lab workflow (used by LabOrderService).
     */
    @Transactional
    public TestOrder createTestOrderEntity(Patient patient, Doctor doctor, IPDAdmission ipdAdmission,
                                           OPDVisit opdVisit, TestMaster testMaster, boolean isPriority) {
        TestOrder order = new TestOrder();
        order.setOrderNumber(orderNumberGenerator.generate());
        order.setPatient(patient);
        order.setTestMaster(testMaster);
        order.setDoctor(doctor);
        order.setIpdAdmission(ipdAdmission);
        order.setOpdVisit(opdVisit);
        order.setStatus(TestStatus.ORDERED);
        order.setOrderedAt(LocalDateTime.now());
        order.setIsPriority(isPriority);

        order = testOrderRepository.save(order);

        if (ipdAdmission != null && !order.getBillingChargePosted()) {
            AdmissionChargeRequestDto chargeRequest = new AdmissionChargeRequestDto();
            chargeRequest.setChargeType(ChargeType.LAB);
            chargeRequest.setAmount(testMaster.getPrice());
            chargeRequest.setDescription("Lab Test: " + testMaster.getTestName() + " (" + testMaster.getTestCode() + ")");
            chargeRequest.setReferenceType("LAB_TEST_ORDER");
            chargeRequest.setReferenceId(order.getId());
            try {
                var charge = admissionChargeService.addCharge(ipdAdmission.getId(), chargeRequest);
                order.setBillingChargePosted(true);
                order.setBillingChargeId(charge.getId());
                testOrderRepository.save(order);
            } catch (Exception e) {
                // Billing can be posted later
            }
        }
        return order;
    }

    @Transactional(readOnly = true)
    public TestOrderResponseDto findById(Long id) {
        TestOrder order = testOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found: " + id));
        return toDto(order);
    }

    @Transactional(readOnly = true)
    public List<TestOrderResponseDto> findByIpdAdmissionId(Long ipdAdmissionId) {
        return testOrderRepository.findByIpdAdmissionIdOrderByOrderedAtDesc(ipdAdmissionId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Pending lab tests for discharge clearance: status not RELEASED (ORDERED, COLLECTED, IN_PROGRESS, COMPLETED, VERIFIED).
     */
    @Transactional(readOnly = true)
    public List<DischargePendingItemDto> getPendingByIpdAdmissionId(Long ipdAdmissionId) {
        return testOrderRepository.findByIpdAdmissionIdOrderByOrderedAtDesc(ipdAdmissionId).stream()
                .filter(o -> o.getStatus() != TestStatus.RELEASED && o.getStatus() != TestStatus.REJECTED && o.getStatus() != TestStatus.CANCELLED)
                .map(o -> new DischargePendingItemDto(
                        o.getId(),
                        o.getTestMaster().getTestName() + " (" + o.getTestMaster().getTestCode() + ") - " + o.getStatus().name(),
                        o.getStatus().name()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TestOrderResponseDto> findByOpdVisitId(Long opdVisitId) {
        return testOrderRepository.findByOpdVisitIdOrderByOrderedAtDesc(opdVisitId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TestOrderResponseDto> findByPatientId(Long patientId) {
        return testOrderRepository.findByPatientIdOrderByOrderedAtDesc(patientId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TestOrderResponseDto> findByStatus(TestStatus status) {
        return testOrderRepository.findByStatusOrderByIsPriorityDescOrderedAtAsc(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TestOrderResponseDto toDto(TestOrder order) {
        TestOrderResponseDto dto = new TestOrderResponseDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setPatientId(order.getPatient().getId());
        dto.setPatientUhid(order.getPatient().getUhid());
        dto.setPatientName(order.getPatient().getFullName());
        dto.setTestMasterId(order.getTestMaster().getId());
        dto.setTestCode(order.getTestMaster().getTestCode());
        dto.setTestName(order.getTestMaster().getTestName());
        dto.setSampleType(order.getTestMaster().getSampleType());
        dto.setDoctorId(order.getDoctor().getId());
        dto.setDoctorName(order.getDoctor().getFullName());
        if (order.getIpdAdmission() != null) {
            dto.setIpdAdmissionId(order.getIpdAdmission().getId());
            dto.setIpdAdmissionNumber(order.getIpdAdmission().getAdmissionNumber());
        }
        if (order.getOpdVisit() != null) {
            dto.setOpdVisitId(order.getOpdVisit().getId());
            dto.setOpdVisitNumber(order.getOpdVisit().getVisitNumber());
        }
        dto.setStatus(order.getStatus());
        dto.setOrderedAt(order.getOrderedAt());
        dto.setSampleCollectedAt(order.getSampleCollectedAt());
        dto.setCollectedBy(order.getCollectedBy());
        dto.setWardName(order.getWardName());
        dto.setBedNumber(order.getBedNumber());
        dto.setResultEnteredAt(order.getResultEnteredAt());
        dto.setResultEnteredBy(order.getResultEnteredBy());
        dto.setVerifiedAt(order.getVerifiedAt());
        dto.setVerifiedBy(order.getVerifiedBy());
        dto.setReleasedAt(order.getReleasedAt());
        dto.setReleasedBy(order.getReleasedBy());
        dto.setTatStartTime(order.getTatStartTime());
        dto.setTatEndTime(order.getTatEndTime());
        dto.setTatStatus(order.getTatStatus());
        dto.setTatBreachReason(order.getTatBreachReason());
        dto.setRejectionReason(order.getRejectionReason());
        dto.setCancellationReason(order.getCancellationReason());
        dto.setClinicalNotes(order.getClinicalNotes());
        dto.setIsPriority(order.getIsPriority());
        dto.setBillingChargePosted(order.getBillingChargePosted());
        dto.setBillingChargeId(order.getBillingChargeId());
        return dto;
    }
}
