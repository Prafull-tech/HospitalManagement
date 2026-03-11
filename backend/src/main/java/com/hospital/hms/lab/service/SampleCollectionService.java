package com.hospital.hms.lab.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.lab.dto.SampleCollectionRequestDto;
import com.hospital.hms.lab.dto.TestOrderResponseDto;
import com.hospital.hms.lab.entity.LabAuditEventType;
import com.hospital.hms.lab.entity.TestOrder;
import com.hospital.hms.lab.entity.TestStatus;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.lab.repository.TestOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Service for sample collection workflow, labeling, and audit trail.
 */
@Service
public class SampleCollectionService {

    private static final Logger log = LoggerFactory.getLogger(SampleCollectionService.class);

    private final TestOrderRepository testOrderRepository;
    private final TestOrderService testOrderService;
    private final LabAuditService labAuditService;

    public SampleCollectionService(TestOrderRepository testOrderRepository, TestOrderService testOrderService,
                                   LabAuditService labAuditService) {
        this.testOrderRepository = testOrderRepository;
        this.testOrderService = testOrderService;
        this.labAuditService = labAuditService;
    }

    /**
     * Mark sample as collected. Updates status, sets TAT start time, and records collection details.
     */
    @Transactional
    public TestOrderResponseDto collectSample(Long testOrderId, SampleCollectionRequestDto request, String collectedBy) {
        TestOrder order = testOrderRepository.findById(testOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found: " + testOrderId));

        if (order.getStatus() != TestStatus.ORDERED) {
            throw new IllegalArgumentException("Sample can only be collected for orders with status ORDERED. Current status: " + order.getStatus());
        }

        order.setStatus(TestStatus.COLLECTED);
        order.setSampleCollectedAt(LocalDateTime.now());
        order.setCollectedBy(collectedBy);
        order.setWardName(request.getWardName());
        order.setBedNumber(request.getBedNumber());
        order.setTatStartTime(Instant.now()); // TAT timer starts at collection

        order = testOrderRepository.save(order);
        labAuditService.log(LabAuditEventType.SAMPLE_COLLECTED, order.getId(), null, null, collectedBy,
                order.getOrderNumber());
        if (log.isInfoEnabled()) {
            MDC.put(MdcKeys.MODULE, "LAB");
            log.info("LAB_AUDIT sample_collection orderId={} orderNumber={} collectedBy={} correlationId={}",
                    order.getId(), order.getOrderNumber(), collectedBy, MDC.get(MdcKeys.CORRELATION_ID));
            MDC.remove(MdcKeys.MODULE);
        }
        return testOrderService.toDto(order);
    }

    /**
     * Reject sample (hemolysed, insufficient, etc.). Updates status and records rejection reason.
     */
    @Transactional
    public TestOrderResponseDto rejectSample(Long testOrderId, String rejectionReason, String performedBy) {
        TestOrder order = testOrderRepository.findById(testOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found: " + testOrderId));

        if (order.getStatus() != TestStatus.COLLECTED && order.getStatus() != TestStatus.ORDERED) {
            throw new IllegalArgumentException("Sample can only be rejected for orders with status ORDERED or COLLECTED");
        }

        order.setStatus(TestStatus.REJECTED);
        order.setRejectionReason(rejectionReason);

        order = testOrderRepository.save(order);
        if (log.isInfoEnabled()) {
            MDC.put(MdcKeys.MODULE, "LAB");
            log.info("LAB_AUDIT sample_rejection orderId={} orderNumber={} performedBy={} reason={} correlationId={}",
                    order.getId(), order.getOrderNumber(), performedBy, rejectionReason, MDC.get(MdcKeys.CORRELATION_ID));
            MDC.remove(MdcKeys.MODULE);
        }
        return testOrderService.toDto(order);
    }
}
