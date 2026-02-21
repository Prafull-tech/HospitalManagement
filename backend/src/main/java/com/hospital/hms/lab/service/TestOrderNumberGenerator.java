package com.hospital.hms.lab.service;

import com.hospital.hms.lab.repository.TestOrderRepository;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates lab test order number in format LAB-YYYY-XXXXXX. DB-independent.
 */
@Component
public class TestOrderNumberGenerator {

    private static final String PREFIX = "LAB";
    private static final int SEQ_LENGTH = 6;

    private final TestOrderRepository testOrderRepository;
    private final AtomicLong sequence = new AtomicLong(0);
    private volatile int lastYear = Year.now().getValue();

    public TestOrderNumberGenerator(TestOrderRepository testOrderRepository) {
        this.testOrderRepository = testOrderRepository;
    }

    public String generate() {
        int currentYear = Year.now().getValue();
        if (currentYear != lastYear) {
            synchronized (this) {
                if (currentYear != lastYear) {
                    lastYear = currentYear;
                    sequence.set(0);
                }
            }
        }
        long seq = sequence.incrementAndGet();
        String orderNumber = String.format("%s-%d-%0" + SEQ_LENGTH + "d", PREFIX, currentYear, seq);
        while (testOrderRepository.findByOrderNumber(orderNumber).isPresent()) {
            seq = sequence.incrementAndGet();
            orderNumber = String.format("%s-%d-%0" + SEQ_LENGTH + "d", PREFIX, currentYear, seq);
        }
        return orderNumber;
    }
}
