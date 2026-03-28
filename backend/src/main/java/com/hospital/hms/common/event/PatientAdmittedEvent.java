package com.hospital.hms.common.event;

public class PatientAdmittedEvent extends DomainEvent {

    private final Long admissionId;
    private final Long patientId;
    private final String admissionNumber;

    public PatientAdmittedEvent(String triggeredBy, Long admissionId,
                                Long patientId, String admissionNumber) {
        super(triggeredBy);
        this.admissionId = admissionId;
        this.patientId = patientId;
        this.admissionNumber = admissionNumber;
    }

    public Long getAdmissionId() { return admissionId; }
    public Long getPatientId() { return patientId; }
    public String getAdmissionNumber() { return admissionNumber; }
}
