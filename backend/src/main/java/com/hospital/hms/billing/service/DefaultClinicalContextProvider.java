package com.hospital.hms.billing.service;

import com.hospital.hms.billing.api.ClinicalContextProvider;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.opd.repository.OPDVisitRepository;
import com.hospital.hms.reception.repository.PatientRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DefaultClinicalContextProvider implements ClinicalContextProvider {

    private final PatientRepository patientRepository;
    private final IPDAdmissionRepository admissionRepository;
    private final OPDVisitRepository opdVisitRepository;

    public DefaultClinicalContextProvider(PatientRepository patientRepository,
                                          IPDAdmissionRepository admissionRepository,
                                          OPDVisitRepository opdVisitRepository) {
        this.patientRepository = patientRepository;
        this.admissionRepository = admissionRepository;
        this.opdVisitRepository = opdVisitRepository;
    }

    @Override
    public Optional<PatientInfo> getPatientById(Long patientId) {
        return patientRepository.findById(patientId)
                .map(p -> new PatientInfo(p.getId(), p.getUhid(), p.getFullName()));
    }

    @Override
    public Optional<AdmissionInfo> getAdmissionById(Long admissionId) {
        return admissionRepository.findById(admissionId)
                .map(a -> new AdmissionInfo(a.getId(), a.getAdmissionNumber(), a.getPatient().getId()));
    }

    @Override
    public Optional<VisitInfo> getVisitById(Long visitId) {
        return opdVisitRepository.findById(visitId)
                .map(v -> new VisitInfo(v.getId(), v.getVisitNumber(), v.getPatient().getId()));
    }
}
