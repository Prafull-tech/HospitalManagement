package com.hospital.hms.opd.service;

import com.hospital.hms.opd.repository.OPDVisitRepository;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates OPD visit number in format OPD-YYYY-XXXXXX. DB-independent.
 */
@Component
public class OPDVisitNumberGenerator {

    private static final String PREFIX = "OPD";
    private static final int SEQ_LENGTH = 6;

    private final OPDVisitRepository visitRepository;
    private final AtomicLong sequence = new AtomicLong(0);
    private volatile int lastYear = Year.now().getValue();

    public OPDVisitNumberGenerator(OPDVisitRepository visitRepository) {
        this.visitRepository = visitRepository;
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
        String visitNumber = String.format("%s-%d-%0" + SEQ_LENGTH + "d", PREFIX, currentYear, seq);
        while (visitRepository.findByVisitNumber(visitNumber).isPresent()) {
            seq = sequence.incrementAndGet();
            visitNumber = String.format("%s-%d-%0" + SEQ_LENGTH + "d", PREFIX, currentYear, seq);
        }
        return visitNumber;
    }
}
