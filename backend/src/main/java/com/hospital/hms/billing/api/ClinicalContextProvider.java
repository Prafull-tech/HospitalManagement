package com.hospital.hms.billing.api;

import java.util.Optional;

/**
 * Provides clinical context (patient, admission, visit info) to billing
 * without billing needing to directly depend on IPD/OPD repositories.
 */
public interface ClinicalContextProvider {

    record PatientInfo(Long patientId, String uhid, String patientName) {}

    record AdmissionInfo(Long admissionId, String admissionNumber, Long patientId) {}

    record VisitInfo(Long visitId, String visitNumber, Long patientId) {}

    Optional<PatientInfo> getPatientById(Long patientId);

    Optional<AdmissionInfo> getAdmissionById(Long admissionId);

    Optional<VisitInfo> getVisitById(Long visitId);
}
