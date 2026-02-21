package com.hospital.hms.lab.service;

import com.hospital.hms.lab.entity.TATStatus;
import com.hospital.hms.lab.entity.TestOrder;
import com.hospital.hms.lab.entity.TestStatus;
import com.hospital.hms.lab.repository.TestOrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Service to track TAT compliance, mark breaches, and feed NABH dashboard.
 * Runs scheduled checks to evaluate TAT status.
 */
@Service
public class TATMonitoringService {

    private final TestOrderRepository testOrderRepository;

    public TATMonitoringService(TestOrderRepository testOrderRepository) {
        this.testOrderRepository = testOrderRepository;
    }

    /**
     * Scheduled task: Check TAT compliance every 5 minutes.
     * Evaluates all tests that have been collected but not yet released.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void checkTATCompliance() {
        List<TestOrder> activeOrders = testOrderRepository.findByStatusIn(
                List.of(TestStatus.COLLECTED, TestStatus.IN_PROGRESS, TestStatus.COMPLETED, TestStatus.VERIFIED)
        );

        for (TestOrder order : activeOrders) {
            if (order.getTatStartTime() == null || order.getTestMaster().getNormalTATMinutes() == null) {
                continue;
            }

            Instant now = Instant.now();
            Duration elapsed = Duration.between(order.getTatStartTime(), now);
            long elapsedMinutes = elapsed.toMinutes();
            int normalTATMinutes = order.getTestMaster().getNormalTATMinutes();

            if (elapsedMinutes > normalTATMinutes && order.getTatStatus() != TATStatus.BREACH) {
                // Mark as breach
                order.setTatStatus(TATStatus.BREACH);
                if (order.getTatBreachReason() == null || order.getTatBreachReason().isEmpty()) {
                    order.setTatBreachReason("TAT exceeded by " + (elapsedMinutes - normalTATMinutes) + " minutes");
                }
                testOrderRepository.save(order);
            } else if (elapsedMinutes <= normalTATMinutes && order.getTatStatus() == null) {
                // Mark as within TAT
                order.setTatStatus(TATStatus.WITHIN_TAT);
                testOrderRepository.save(order);
            }
        }
    }

    /**
     * Evaluate TAT for a specific test order and update status.
     */
    @Transactional
    public void evaluateTAT(Long testOrderId) {
        TestOrder order = testOrderRepository.findById(testOrderId)
                .orElseThrow(() -> new RuntimeException("Test order not found: " + testOrderId));

        if (order.getTatStartTime() == null || order.getTatEndTime() == null) {
            return; // TAT not yet complete
        }

        Duration actualTAT = Duration.between(order.getTatStartTime(), order.getTatEndTime());
        long actualMinutes = actualTAT.toMinutes();
        int normalTATMinutes = order.getTestMaster().getNormalTATMinutes();

        if (actualMinutes > normalTATMinutes) {
            order.setTatStatus(TATStatus.BREACH);
            if (order.getTatBreachReason() == null || order.getTatBreachReason().isEmpty()) {
                order.setTatBreachReason("TAT exceeded by " + (actualMinutes - normalTATMinutes) + " minutes");
            }
        } else {
            order.setTatStatus(TATStatus.WITHIN_TAT);
        }

        testOrderRepository.save(order);
    }
}
