package com.hospital.hms.reception.service;

import com.hospital.hms.reception.repository.PatientRepository;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates UHID in format HMS-YYYY-XXXXXX. DB-independent; uses in-memory
 * sequence per year for uniqueness within the application instance.
 * For multi-instance production, consider a DB sequence or distributed ID.
 */
@Component
public class UhidGenerator {

    private static final String PREFIX = "HMS";
    private static final int SEQ_LENGTH = 6;

    private final PatientRepository patientRepository;
    private final AtomicLong sequence = new AtomicLong(0);
    private volatile int lastYear = Year.now().getValue();

    public UhidGenerator(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
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
        String uhid = String.format("%s-%d-%0" + SEQ_LENGTH + "d", PREFIX, currentYear, seq);
        // Ensure uniqueness: if already exists (e.g. from DB seed), bump until unique
        while (patientRepository.findByUhid(uhid).isPresent()) {
            seq = sequence.incrementAndGet();
            uhid = String.format("%s-%d-%0" + SEQ_LENGTH + "d", PREFIX, currentYear, seq);
        }
        return uhid;
    }
}
