package com.hospital.hms.lab.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.lab.dto.LabResultRequestDto;
import com.hospital.hms.lab.dto.LabResultResponseDto;
import com.hospital.hms.lab.entity.LabResult;
import com.hospital.hms.lab.entity.TestOrder;
import com.hospital.hms.lab.entity.TestStatus;
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

    public LabProcessingService(TestOrderRepository testOrderRepository, LabResultRepository labResultRepository) {
        this.testOrderRepository = testOrderRepository;
        this.labResultRepository = labResultRepository;
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
        }

        return results.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LabResultResponseDto> findByTestOrderId(Long testOrderId) {
        return labResultRepository.findByTestOrderIdOrderByParameterNameAsc(testOrderId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private LabResultResponseDto toDto(LabResult r) {
        LabResultResponseDto dto = new LabResultResponseDto();
        dto.setId(r.getId());
        dto.setTestOrderId(r.getTestOrder().getId());
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
