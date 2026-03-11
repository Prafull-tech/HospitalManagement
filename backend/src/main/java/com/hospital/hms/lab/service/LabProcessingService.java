package com.hospital.hms.lab.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.lab.dto.LabResultEntryRequestDto;
import com.hospital.hms.lab.dto.LabResultRequestDto;
import com.hospital.hms.lab.dto.LabResultResponseDto;
import com.hospital.hms.lab.entity.LabAuditEventType;
import com.hospital.hms.lab.entity.LabOrderItem;
import com.hospital.hms.lab.entity.LabOrderItemStatus;
import com.hospital.hms.lab.entity.LabResult;
import com.hospital.hms.lab.entity.TestOrder;
import com.hospital.hms.lab.entity.TestStatus;
import com.hospital.hms.lab.repository.LabOrderItemRepository;
import com.hospital.hms.lab.repository.LabResultRepository;
import com.hospital.hms.lab.repository.TestOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for lab result entry, status updates, and TAT timer management.
 */
@Service
public class LabProcessingService {

    private final TestOrderRepository testOrderRepository;
    private final LabResultRepository labResultRepository;
    private final LabOrderItemRepository labOrderItemRepository;
    private final LabAuditService labAuditService;

    public LabProcessingService(TestOrderRepository testOrderRepository, LabResultRepository labResultRepository,
                               LabOrderItemRepository labOrderItemRepository, LabAuditService labAuditService) {
        this.testOrderRepository = testOrderRepository;
        this.labResultRepository = labResultRepository;
        this.labOrderItemRepository = labOrderItemRepository;
        this.labAuditService = labAuditService;
    }

    /**
     * Start processing a collected sample. Transitions COLLECTED -> IN_PROGRESS.
     */
    @Transactional
    public void startProcessing(Long testOrderId, String performedBy) {
        TestOrder order = testOrderRepository.findById(testOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found: " + testOrderId));
        if (order.getStatus() != TestStatus.COLLECTED) {
            throw new IllegalArgumentException("Only COLLECTED samples can start processing. Current status: " + order.getStatus());
        }
        order.setStatus(TestStatus.IN_PROGRESS);
        testOrderRepository.save(order);
    }

    /**
     * Enter lab results. Updates test order status to IN_PROGRESS, then COMPLETED.
     */
    @Transactional
    public List<LabResultResponseDto> enterResults(LabResultRequestDto request, String enteredBy) {
        TestOrder order = testOrderRepository.findById(request.getTestOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found: " + request.getTestOrderId()));

        if (order.getStatus() != TestStatus.COLLECTED && order.getStatus() != TestStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Results can only be entered for orders with status COLLECTED or IN_PROGRESS. Current status: " + order.getStatus());
        }

        // Update order status
        if (order.getStatus() == TestStatus.COLLECTED) {
            order.setStatus(TestStatus.IN_PROGRESS);
        }
        order.setResultEnteredAt(LocalDateTime.now());
        order.setResultEnteredBy(enteredBy);
        testOrderRepository.save(order);

        // Save results
        List<LabResult> results = new java.util.ArrayList<>();
        if (request.getParameters() != null) {
            for (LabResultRequestDto.ResultParameterDto param : request.getParameters()) {
                LabResult result = new LabResult();
                result.setTestOrder(order);
                result.setParameterName(param.getParameterName());
                result.setResultValue(param.getResultValue());
                result.setUnit(param.getUnit());
                result.setNormalRange(param.getNormalRange());
                result.setFlag(param.getFlag());
                result.setIsCritical(param.getIsCritical() != null ? param.getIsCritical() : Boolean.FALSE);
                result.setEnteredAt(LocalDateTime.now());
                result.setEnteredBy(enteredBy);
                result.setRemarks(request.getRemarks());
                results.add(labResultRepository.save(result));
            }
        }

        // Mark as completed if results entered
        if (!results.isEmpty()) {
            order.setStatus(TestStatus.COMPLETED);
            testOrderRepository.save(order);
            labAuditService.log(LabAuditEventType.RESULT_ENTERED, order.getId(), null, null, enteredBy,
                    order.getOrderNumber());
        }

        return results.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Enter a single result by orderItemId. Updates LabOrderItem and TestOrder status to COMPLETED.
     */
    @Transactional
    public LabResultResponseDto enterResultByOrderItem(LabResultEntryRequestDto request, String enteredBy) {
        LabOrderItem item = labOrderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Lab order item not found: " + request.getOrderItemId()));
        TestOrder testOrder = item.getTestOrder();
        if (testOrder == null) {
            throw new IllegalArgumentException("Lab order item has no linked test order");
        }
        if (testOrder.getStatus() != TestStatus.COLLECTED && testOrder.getStatus() != TestStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Results can only be entered for COLLECTED or IN_PROGRESS items. Current: " + testOrder.getStatus());
        }

        LabResult result = new LabResult();
        result.setOrderItem(item);
        result.setTestOrder(testOrder);
        result.setResultValue(request.getResultValue());
        result.setUnit(request.getUnit());
        result.setNormalRange(request.getNormalRange());
        result.setRemarks(request.getRemarks());
        result.setEnteredAt(LocalDateTime.now());
        result.setEnteredBy(enteredBy);
        result = labResultRepository.save(result);

        testOrder.setStatus(TestStatus.COMPLETED);
        testOrder.setResultEnteredAt(LocalDateTime.now());
        testOrder.setResultEnteredBy(enteredBy);
        testOrderRepository.save(testOrder);

        item.setStatus(LabOrderItemStatus.COMPLETED);
        labOrderItemRepository.save(item);

        labAuditService.log(LabAuditEventType.RESULT_ENTERED, testOrder.getId(), item.getId(), null, enteredBy,
                testOrder.getOrderNumber());

        return toDto(result);
    }

    @Transactional(readOnly = true)
    public List<LabResultResponseDto> findByTestOrderId(Long testOrderId) {
        return labResultRepository.findByTestOrder_IdOrderByParameterNameAsc(testOrderId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LabResultResponseDto> findByOrderItemId(Long orderItemId) {
        return labResultRepository.findByOrderItem_IdOrderByIdAsc(orderItemId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private LabResultResponseDto toDto(LabResult r) {
        LabResultResponseDto dto = new LabResultResponseDto();
        dto.setId(r.getId());
        if (r.getTestOrder() != null) {
            dto.setTestOrderId(r.getTestOrder().getId());
        }
        if (r.getOrderItem() != null) {
            dto.setOrderItemId(r.getOrderItem().getId());
        }
        dto.setParameterName(r.getParameterName());
        dto.setResultValue(r.getResultValue());
        dto.setUnit(r.getUnit());
        dto.setNormalRange(r.getNormalRange());
        dto.setFlag(r.getFlag());
        dto.setEnteredAt(r.getEnteredAt());
        dto.setEnteredBy(r.getEnteredBy());
        dto.setRemarks(r.getRemarks());
        dto.setIsCritical(r.getIsCritical());
        return dto;
    }
}
