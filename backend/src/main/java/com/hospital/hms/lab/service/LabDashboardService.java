package com.hospital.hms.lab.service;

import com.hospital.hms.lab.dto.LabDashboardMetricsDto;
import com.hospital.hms.lab.dto.LabDashboardOverviewDto;
import com.hospital.hms.lab.dto.LabDashboardSummaryDto;
import com.hospital.hms.lab.dto.LabTodaySummaryDto;
import com.hospital.hms.lab.dto.TestOrderResponseDto;
import com.hospital.hms.lab.entity.TATStatus;
import com.hospital.hms.lab.entity.TestStatus;
import com.hospital.hms.lab.repository.TestOrderRepository;
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

    public LabDashboardService(TestOrderRepository testOrderRepository, TestOrderService testOrderService) {
        this.testOrderRepository = testOrderRepository;
        this.testOrderService = testOrderService;
    }

    @Transactional(readOnly = true)
    public LabDashboardSummaryDto getDashboardSummary() {
        LabDashboardSummaryDto summary = new LabDashboardSummaryDto();

        // Pending collection (ORDERED status)
        List<TestOrderResponseDto> pendingCollection = testOrderRepository
                .findByStatusOrderByOrderedAtAsc(TestStatus.ORDERED).stream()
                .map(testOrderService::toDto)
                .limit(50) // Limit for performance
                .collect(Collectors.toList());
        summary.setPendingCollection(pendingCollection);
        summary.setPendingCollectionCount((long) pendingCollection.size());

        // Pending verification (COMPLETED status)
        List<TestOrderResponseDto> pendingVerification = testOrderRepository
                .findPendingVerification(TestStatus.COMPLETED).stream()
                .map(testOrderService::toDto)
                .limit(50)
                .collect(Collectors.toList());
        summary.setPendingVerification(pendingVerification);
        summary.setPendingVerificationCount((long) pendingVerification.size());

        // TAT breaches
        List<TestOrderResponseDto> tatBreaches = testOrderRepository
                .findTATBreaches(List.of(TestStatus.COLLECTED, TestStatus.IN_PROGRESS, TestStatus.COMPLETED, TestStatus.VERIFIED)).stream()
                .map(testOrderService::toDto)
                .limit(50)
                .collect(Collectors.toList());
        summary.setTatBreaches(tatBreaches);
        summary.setTatBreachCount((long) tatBreaches.size());

        // Emergency samples pending collection
        List<TestOrderResponseDto> emergencySamples = testOrderRepository
                .findEmergencySamplesPendingCollection(TestStatus.ORDERED).stream()
                .map(testOrderService::toDto)
                .limit(50)
                .collect(Collectors.toList());
        summary.setEmergencySamples(emergencySamples);
        summary.setEmergencySamplesCount((long) emergencySamples.size());

        // Completed today (released today)
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        Long completedToday = testOrderRepository.countReleasedBetween(TestStatus.RELEASED, startOfDay, endOfDay);
        summary.setCompletedTodayCount(completedToday != null ? completedToday : 0L);

        return summary;
    }

    @Transactional(readOnly = true)
    public LabDashboardMetricsDto getMetrics() {
        LabDashboardMetricsDto dto = new LabDashboardMetricsDto();
        dto.setPendingCollection(testOrderRepository.countByStatus(TestStatus.ORDERED));
        dto.setPendingVerification(testOrderRepository.countPendingVerification(TestStatus.COMPLETED));
        dto.setTatBreaches(testOrderRepository.countTatBreaches(
                List.of(TestStatus.COLLECTED, TestStatus.IN_PROGRESS, TestStatus.COMPLETED, TestStatus.VERIFIED)));
        dto.setEmergencySamples(testOrderRepository.countEmergencySamplesPendingCollection(TestStatus.ORDERED));
        return dto;
    }

    @Transactional(readOnly = true)
    public LabDashboardOverviewDto getOverview() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        Long ordered = testOrderRepository.countOrderedBetween(startOfDay, endOfDay);
        Long collected = testOrderRepository.countCollectedBetween(startOfDay, endOfDay);
        Long completed = testOrderRepository.countResultEnteredBetween(startOfDay, endOfDay);
        Long verified = testOrderRepository.countVerifiedBetween(startOfDay, endOfDay);
        Long releasedTotal = testOrderRepository.countReleasedBetween(TestStatus.RELEASED, startOfDay, endOfDay);
        Long withinTat = testOrderRepository.countReleasedWithTatStatusBetween(startOfDay, endOfDay, TATStatus.WITHIN_TAT);

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
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        Long completed = testOrderRepository.countReleasedBetween(TestStatus.RELEASED, startOfDay, endOfDay);
        long pending = testOrderRepository.countByStatus(TestStatus.ORDERED);
        Long releasedTotal = testOrderRepository.countReleasedBetween(TestStatus.RELEASED, startOfDay, endOfDay);
        Long withinTat = testOrderRepository.countReleasedWithTatStatusBetween(startOfDay, endOfDay, TATStatus.WITHIN_TAT);
        Long emergencyReleased = testOrderRepository.countReleasedBetweenWithPriority(TestStatus.RELEASED, startOfDay, endOfDay);

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
