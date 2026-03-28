package com.hospital.hms.common.event;

import java.math.BigDecimal;

public class OpdVisitCompletedEvent extends DomainEvent {

    private final Long opdVisitId;
    private final BigDecimal consultationFee;
    private final String doctorDisplayName;

    public OpdVisitCompletedEvent(String triggeredBy, Long opdVisitId,
                                  BigDecimal consultationFee, String doctorDisplayName) {
        super(triggeredBy);
        this.opdVisitId = opdVisitId;
        this.consultationFee = consultationFee;
        this.doctorDisplayName = doctorDisplayName;
    }

    public Long getOpdVisitId() { return opdVisitId; }
    public BigDecimal getConsultationFee() { return consultationFee; }
    public String getDoctorDisplayName() { return doctorDisplayName; }
}
