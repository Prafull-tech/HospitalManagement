package com.hospital.hms.prescription.service;

import com.hospital.hms.prescription.repository.PrescriptionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PrescriptionNumberGenerator {

    private static final String PREFIX = "RX";
    private static final int SEQ_LENGTH = 6;

    private final PrescriptionRepository prescriptionRepository;
    private final AtomicLong sequence = new AtomicLong(0);
    private volatile int lastDayOfYear = LocalDate.now().getDayOfYear();
    private volatile int lastYear = LocalDate.now().getYear();

    public PrescriptionNumberGenerator(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public String generate() {
        LocalDate currentDate = LocalDate.now();
        if (currentDate.getYear() != lastYear || currentDate.getDayOfYear() != lastDayOfYear) {
            synchronized (this) {
                if (currentDate.getYear() != lastYear || currentDate.getDayOfYear() != lastDayOfYear) {
                    lastYear = currentDate.getYear();
                    lastDayOfYear = currentDate.getDayOfYear();
                    sequence.set(0);
                }
            }
        }

        long nextValue = sequence.incrementAndGet();
        String datePart = currentDate.toString().replace("-", "");
        String prescriptionNumber = String.format("%s-%s-%0" + SEQ_LENGTH + "d", PREFIX, datePart, nextValue);
        while (prescriptionRepository.findByPrescriptionNumber(prescriptionNumber).isPresent()) {
            nextValue = sequence.incrementAndGet();
            prescriptionNumber = String.format("%s-%s-%0" + SEQ_LENGTH + "d", PREFIX, datePart, nextValue);
        }
        return prescriptionNumber;
    }
}