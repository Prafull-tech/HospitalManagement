package com.hospital.hms.lab.service;

import com.hospital.hms.lab.dto.LabDashboardMetricsDto;
import com.hospital.hms.lab.dto.LabDashboardOverviewDto;
import com.hospital.hms.lab.dto.LabDashboardResponseDto;
import com.hospital.hms.lab.dto.LabDashboardSummaryDto;
import com.hospital.hms.lab.dto.LabTodaySummaryDto;
import com.hospital.hms.lab.dto.TestOrderResponseDto;
import com.hospital.hms.lab.entity.TATStatus;
import com.hospital.hms.lab.entity.TestStatus;
import com.hospital.hms.lab.repository.TestOrderRepository;
import com.hospital.hms.tenant.service.TenantContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to aggregate dashboard data: pending samples, completed tests, TAT breaches, emergency samples.
 */
@Service
public class LabDashboardService {

    private final TestOrderRepository testOrderRepository;
    private final TestOrderService testOrderService;
    private final TenantContextService tenantContextService;

    public LabDashboardService(TestOrderRepository testOrderRepository, TestOrderService testOrderService, TenantContextService tenantContextService) {
        this.testOrderRepository = testOrderRepository;
        this.testOrderService = testOrderService;
        this.tenantContextService = tenantContextService;
    }

    @Transactional(readOnly = true)
    public LabDashboardSummaryDto getDashboardSummary() {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        LabDashboardSummaryDto summary = new LabDashboardSummaryDto();

        // Pending collection (ORDERED status) - emergency first
        List<TestOrderResponseDto> pendingCollection = testOrderRepository
                .findByHospitalIdAndStatusOrderByIsPriorityDescOrderedAtAsc(hospitalId, TestStatus.ORDERED).stream()
                .map(testOrderService::toDto)
                .limit(50) // Limit for performance
                .collect(Collectors.toList());
        summary.setPendingCollection(pendingCollection);
        summary.setPendingCollectionCount((long) pendingCollection.size());

        // Pending processing (COLLECTED status) - emergency first
        List<TestOrderResponseDto> pendingProcessing = testOrderRepository
                .findByHospitalIdAndStatusOrderByIsPriorityDescOrderedAtAsc(hospitalId, TestStatus.COLLECTED).stream()
                .map(testOrderService::toDto)
                .limit(50)
                .collect(Collectors.toList());
        summary.setPendingProcessing(pendingProcessing);
        summary.setPendingProcessingCount((long) pendingProcessing.size());

        // Pending verification (COMPLETED status)
        List<TestOrderResponseDto> pendingVerification = testOrderRepository
                .findPendingVerificationByHospitalId(hospitalId, TestStatus.COMPLETED).stream()
                .map(testOrderService::toDto)
                .limit(50)
                .collect(Collectors.toList());
        summary.setPendingVerification(pendingVerification);
        summary.setPendingVerificationCount((long) pendingVerification.size());

        // TAT breaches
        List<TestOrderResponseDto> tatBreaches = testOrderRepository
                .findTATBreachesByHospitalId(hospitalId, List.of(TestStatus.COLLECTED, TestStatus.IN_PROGRESS, TestStatus.COMPLETED, TestStatus.VERIFIED)).stream()
                .map(testOrderService::toDto)
                .limit(50)
                .collect(Collectors.toList());
        summary.setTatBreaches(tatBreaches);
        summary.setTatBreachCount((long) tatBreaches.size());

        // Emergency samples pending collection
        List<TestOrderResponseDto> emergencySamples = testOrderRepository
                .findEmergencySamplesPendingCollectionByHospitalId(hospitalId, TestStatus.ORDERED).stream()
                .map(testOrderService::toDto)
                .limit(50)
                .collect(Collectors.toList());
        summary.setEmergencySamples(emergencySamples);
        summary.setEmergencySamplesCount((long) emergencySamples.size());

        // Completed today (released today)
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        Long completedToday = testOrderRepository.countReleasedBetweenByHospitalId(hospitalId, TestStatus.RELEASED, startOfDay, endOfDay);
        summary.setCompletedTodayCount(completedToday != null ? completedToday : 0L);

        return summary;
    }

    /**
     * Unified dashboard API: counts + today's activity.
     */
    @Transactional(readOnly = true)
    public LabDashboardResponseDto getDashboard() {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        LabDashboardResponseDto dto = new LabDashboardResponseDto();
        dto.setPendingCollection(testOrderRepository.countByStatusAndHospitalId(hospitalId, TestStatus.ORDERED));
        dto.setPendingProcessing(testOrderRepository.countByStatusAndHospitalId(hospitalId, TestStatus.COLLECTED));
        dto.setPendingVerification(testOrderRepository.countPendingVerificationByHospitalId(hospitalId, TestStatus.COMPLETED));
        dto.setTatBreaches(testOrderRepository.countTatBreachesByHospitalId(hospitalId,
                List.of(TestStatus.COLLECTED, TestStatus.IN_PROGRESS, TestStatus.COMPLETED, TestStatus.VERIFIED)));
        dto.setEmergencySamples(testOrderRepository.countEmergencySamplesPendingCollectionByHospitalId(hospitalId, TestStatus.ORDERED));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        Long ordered = testOrderRepository.countOrderedBetweenByHospitalId(hospitalId, startOfDay, endOfDay);
        Long collected = testOrderRepository.countCollectedBetweenByHospitalId(hospitalId, startOfDay, endOfDay);
        Long completed = testOrderRepository.countResultEnteredBetweenByHospitalId(hospitalId, startOfDay, endOfDay);
        Long verified = testOrderRepository.countVerifiedBetweenByHospitalId(hospitalId, startOfDay, endOfDay);
        Long releasedTotal = testOrderRepository.countReleasedBetweenByHospitalId(hospitalId, TestStatus.RELEASED, startOfDay, endOfDay);
        Long withinTat = testOrderRepository.countReleasedWithTatStatusBetweenByHospitalId(hospitalId, startOfDay, endOfDay, TATStatus.WITHIN_TAT);

        dto.setTodayOrdered(ordered != null ? ordered : 0L);
        dto.setTodayCollected(collected != null ? collected : 0L);
        dto.setTodayCompleted(completed != null ? completed : 0L);
        dto.setTodayVerified(verified != null ? verified : 0L);
        if (releasedTotal != null && releasedTotal > 0 && withinTat != null) {
            dto.setTatCompliancePercent(100.0 * withinTat / releasedTotal);
        } else {
            dto.setTatCompliancePercent(100.0);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public LabDashboardMetricsDto getMetrics() {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        LabDashboardMetricsDto dto = new LabDashboardMetricsDto();
        dto.setPendingCollection(testOrderRepository.countByStatusAndHospitalId(hospitalId, TestStatus.ORDERED));
        dto.setPendingVerification(testOrderRepository.countPendingVerificationByHospitalId(hospitalId, TestStatus.COMPLETED));
        dto.setTatBreaches(testOrderRepository.countTatBreachesByHospitalId(hospitalId,
                List.of(TestStatus.COLLECTED, TestStatus.IN_PROGRESS, TestStatus.COMPLETED, TestStatus.VERIFIED)));
        dto.setEmergencySamples(testOrderRepository.countEmergencySamplesPendingCollectionByHospitalId(hospitalId, TestStatus.ORDERED));
        return dto;
    }

    @Transactional(readOnly = true)
    public LabDashboardOverviewDto getOverview() {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        Long ordered = testOrderRepository.countOrderedBetweenByHospitalId(hospitalId, startOfDay, endOfDay);
        Long collected = testOrderRepository.countCollectedBetweenByHospitalId(hospitalId, startOfDay, endOfDay);
        Long completed = testOrderRepository.countResultEnteredBetweenByHospitalId(hospitalId, startOfDay, endOfDay);
        Long verified = testOrderRepository.countVerifiedBetweenByHospitalId(hospitalId, startOfDay, endOfDay);
        Long releasedTotal = testOrderRepository.countReleasedBetweenByHospitalId(hospitalId, TestStatus.RELEASED, startOfDay, endOfDay);
        Long withinTat = testOrderRepository.countReleasedWithTatStatusBetweenByHospitalId(hospitalId, startOfDay, endOfDay, TATStatus.WITHIN_TAT);

        LabDashboardOverviewDto dto = new LabDashboardOverviewDto();
        dto.setTotalOrderedToday(ordered != null ? ordered : 0L);
        dto.setTestsCollectedToday(collected != null ? collected : 0L);
        dto.setTestsCompletedToday(completed != null ? completed : 0L);
        dto.setTestsVerifiedToday(verified != null ? verified : 0L);
        if (releasedTotal != null && releasedTotal > 0 && withinTat != null) {
            dto.setTatCompliancePercent(100.0 * withinTat / releasedTotal);
        } else {
            dto.setTatCompliancePercent(100.0);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public LabTodaySummaryDto getTodaySummary() {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        Long completed = testOrderRepository.countReleasedBetweenByHospitalId(hospitalId, TestStatus.RELEASED, startOfDay, endOfDay);
        long pending = testOrderRepository.countByStatusAndHospitalId(hospitalId, TestStatus.ORDERED);
        Long releasedTotal = testOrderRepository.countReleasedBetweenByHospitalId(hospitalId, TestStatus.RELEASED, startOfDay, endOfDay);
        Long withinTat = testOrderRepository.countReleasedWithTatStatusBetweenByHospitalId(hospitalId, startOfDay, endOfDay, TATStatus.WITHIN_TAT);
        Long emergencyReleased = testOrderRepository.countReleasedBetweenWithPriorityByHospitalId(hospitalId, TestStatus.RELEASED, startOfDay, endOfDay);

        LabTodaySummaryDto dto = new LabTodaySummaryDto();
        dto.setDate(today.format(DateTimeFormatter.ISO_LOCAL_DATE));
        dto.setCompletedTestsToday(completed != null ? completed : 0L);
        dto.setPendingSamplesToday(pending);
        if (releasedTotal != null && releasedTotal > 0 && withinTat != null) {
            dto.setTatCompliancePercent(100.0 * withinTat / releasedTotal);
        } else {
            dto.setTatCompliancePercent(100.0);
        }
        dto.setEmergencyTestsHandledToday(emergencyReleased != null ? emergencyReleased : 0L);
        return dto;
    }
}
