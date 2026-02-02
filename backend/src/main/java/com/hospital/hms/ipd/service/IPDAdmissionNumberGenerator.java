package com.hospital.hms.ipd.service;

import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates IPD admission number in format IPD-YYYY-XXXXXX. DB-independent.
 */
@Component
public class IPDAdmissionNumberGenerator {

    private static final String PREFIX = "IPD";
    private static final int SEQ_LENGTH = 6;

    private final IPDAdmissionRepository admissionRepository;
    private final AtomicLong sequence = new AtomicLong(0);
    private volatile int lastYear = Year.now().getValue();

    public IPDAdmissionNumberGenerator(IPDAdmissionRepository admissionRepository) {
        this.admissionRepository = admissionRepository;
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
        String admissionNumber = String.format("%s-%d-%0" + SEQ_LENGTH + "d", PREFIX, currentYear, seq);
        while (admissionRepository.findByAdmissionNumber(admissionNumber).isPresent()) {
            seq = sequence.incrementAndGet();
            admissionNumber = String.format("%s-%d-%0" + SEQ_LENGTH + "d", PREFIX, currentYear, seq);
        }
        return admissionNumber;
    }
}
