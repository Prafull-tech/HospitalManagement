package com.hospital.hms.reception.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.reception.dto.PatientCardDto;
import com.hospital.hms.reception.dto.PatientRequestDto;
import com.hospital.hms.reception.dto.PatientResponseDto;
import com.hospital.hms.reception.entity.Patient;
import com.hospital.hms.reception.repository.PatientRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reception patient service. UHID and Registration Number are generated here; no billing/diagnosis logic.
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UhidGenerator uhidGenerator;
    private final RegistrationNumberGenerator registrationNumberGenerator;

    public PatientService(PatientRepository patientRepository, UhidGenerator uhidGenerator,
                          RegistrationNumberGenerator registrationNumberGenerator) {
        this.patientRepository = patientRepository;
        this.uhidGenerator = uhidGenerator;
        this.registrationNumberGenerator = registrationNumberGenerator;
    }

    @Transactional
    public PatientResponseDto register(PatientRequestDto request) {
        Patient patient = new Patient();
        patient.setUhid(uhidGenerator.generate());
        patient.setRegistrationNumber(registrationNumberGenerator.generate());
        patient.setRegistrationDate(LocalDateTime.now());
        patient.setFullName(request.getFullName().trim());
        patient.setIdProofType(request.getIdProofType() != null ? request.getIdProofType().trim() : null);
        patient.setIdProofNumber(request.getIdProofNumber() != null ? request.getIdProofNumber().trim() : null);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setAge(request.getAge());
        patient.setAgeYears(request.getAgeYears());
        patient.setAgeMonths(request.getAgeMonths());
        patient.setAgeDays(request.getAgeDays());
        patient.setGender(request.getGender().trim());
        patient.setWeightKg(request.getWeightKg());
        patient.setHeightCm(request.getHeightCm());
        patient.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        patient.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        patient.setState(request.getState() != null ? request.getState().trim() : null);
        patient.setCity(request.getCity() != null ? request.getCity().trim() : null);
        patient.setDistrict(request.getDistrict() != null ? request.getDistrict().trim() : null);
        patient.setFatherHusbandName(request.getFatherHusbandName() != null ? request.getFatherHusbandName().trim() : null);
        patient.setReferredBy(request.getReferredBy() != null ? request.getReferredBy().trim() : null);
        patient.setReferredName(request.getReferredName() != null ? request.getReferredName().trim() : null);
        patient.setReferredPhone(request.getReferredPhone() != null ? request.getReferredPhone().trim() : null);
        patient.setConsultantName(request.getConsultantName() != null ? request.getConsultantName().trim() : null);
        patient.setSpecialization(request.getSpecialization() != null ? request.getSpecialization().trim() : null);
        patient.setOrganisationType(request.getOrganisationType() != null ? request.getOrganisationType().trim() : null);
        patient.setOrganisationName(request.getOrganisationName() != null ? request.getOrganisationName().trim() : null);
        patient.setRemarks(request.getRemarks() != null ? request.getRemarks().trim() : null);
        patient = patientRepository.save(patient);
        return toResponse(patient);
    }

    public PatientResponseDto getById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + id));
        return toResponse(patient);
    }

    public PatientResponseDto getByUhid(String uhid) {
        Patient patient = patientRepository.findByUhid(uhid)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UHID: " + uhid));
        return toResponse(patient);
    }

    @Transactional
    public PatientResponseDto update(Long id, PatientRequestDto request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + id));
        patient.setFullName(request.getFullName().trim());
        patient.setIdProofType(request.getIdProofType() != null ? request.getIdProofType().trim() : null);
        patient.setIdProofNumber(request.getIdProofNumber() != null ? request.getIdProofNumber().trim() : null);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setAge(request.getAge());
        patient.setAgeYears(request.getAgeYears());
        patient.setAgeMonths(request.getAgeMonths());
        patient.setAgeDays(request.getAgeDays());
        patient.setGender(request.getGender().trim());
        patient.setWeightKg(request.getWeightKg());
        patient.setHeightCm(request.getHeightCm());
        patient.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        patient.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        patient.setState(request.getState() != null ? request.getState().trim() : null);
        patient.setCity(request.getCity() != null ? request.getCity().trim() : null);
        patient.setDistrict(request.getDistrict() != null ? request.getDistrict().trim() : null);
        patient.setFatherHusbandName(request.getFatherHusbandName() != null ? request.getFatherHusbandName().trim() : null);
        patient.setReferredBy(request.getReferredBy() != null ? request.getReferredBy().trim() : null);
        patient.setReferredName(request.getReferredName() != null ? request.getReferredName().trim() : null);
        patient.setReferredPhone(request.getReferredPhone() != null ? request.getReferredPhone().trim() : null);
        patient.setConsultantName(request.getConsultantName() != null ? request.getConsultantName().trim() : null);
        patient.setSpecialization(request.getSpecialization() != null ? request.getSpecialization().trim() : null);
        patient.setOrganisationType(request.getOrganisationType() != null ? request.getOrganisationType().trim() : null);
        patient.setOrganisationName(request.getOrganisationName() != null ? request.getOrganisationName().trim() : null);
        patient.setRemarks(request.getRemarks() != null ? request.getRemarks().trim() : null);
        patient = patientRepository.save(patient);
        return toResponse(patient);
    }

    /**
     * Returns print-ready patient card data for GET /api/patients/{uhid}/card.
     */
    public PatientCardDto getCardByUhid(String uhid) {
        Patient patient = patientRepository.findByUhid(uhid)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UHID: " + uhid));
        return toCard(patient);
    }

    private PatientCardDto toCard(Patient p) {
        PatientCardDto card = new PatientCardDto();
        card.setUhid(p.getUhid());
        card.setRegistrationNumber(p.getRegistrationNumber());
        card.setRegistrationDate(p.getRegistrationDate());
        card.setFullName(p.getFullName());
        card.setAge(p.getAge());
        card.setAgeDisplay(buildAgeDisplay(p));
        card.setGender(p.getGender());
        card.setDateOfBirth(p.getDateOfBirth());
        card.setPhone(p.getPhone());
        card.setAddress(p.getAddress());
        card.setCity(p.getCity());
        card.setState(p.getState());
        card.setDistrict(p.getDistrict());
        card.setIdProofType(p.getIdProofType());
        card.setIdProofNumber(p.getIdProofNumber());
        card.setFatherHusbandName(p.getFatherHusbandName());
        return card;
    }

    private static String buildAgeDisplay(Patient p) {
        if (p.getAgeYears() != null && p.getAgeYears() > 0) {
            if (p.getAgeMonths() != null && p.getAgeMonths() > 0) {
                return p.getAgeYears() + " Y " + p.getAgeMonths() + " M";
            }
            if (p.getAgeDays() != null && p.getAgeDays() > 0) {
                return p.getAgeYears() + " Y " + p.getAgeDays() + " D";
            }
            return p.getAgeYears() + " Y";
        }
        if (p.getAgeMonths() != null && p.getAgeMonths() > 0) {
            return p.getAgeMonths() + " M";
        }
        if (p.getAgeDays() != null && p.getAgeDays() > 0) {
            return p.getAgeDays() + " D";
        }
        return p.getAge() != null ? p.getAge() + " Y" : "";
    }

    /**
     * Single-query search: by ID (numeric), phone, UHID (exact), or name (contains).
     * Tries ID and phone if q is numeric; else UHID exact and name contains.
     */
    public List<PatientResponseDto> searchByQuery(String q) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        String trimmed = q.trim();
        if (trimmed.matches("\\d+")) {
            long id = Long.parseLong(trimmed);
            List<PatientResponseDto> byId = patientRepository.findById(id)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            List<PatientResponseDto> byPhone = patientRepository.findByPhone(trimmed)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            return java.util.stream.Stream.concat(byId.stream(), byPhone.stream())
                    .collect(Collectors.toMap(PatientResponseDto::getId, p -> p, (a, b) -> a))
                    .values().stream()
                    .collect(Collectors.toList());
        }
        if (trimmed.matches("^[0-9+\\-\\s]+$")) {
            return patientRepository.findByPhone(trimmed)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        List<Patient> byUhid = patientRepository.findByUhid(trimmed).stream().toList();
        List<Patient> byName = patientRepository.findByFullNameContainingIgnoreCase(trimmed);
        return java.util.stream.Stream.concat(byUhid.stream(), byName.stream())
                .distinct()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PatientResponseDto> search(String uhid, String phone, String name) {
        if (uhid != null && !uhid.isBlank()) {
            return patientRepository.findByUhid(uhid.trim())
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        if (phone != null && !phone.isBlank()) {
            return patientRepository.findByPhone(phone.trim())
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        if (name != null && !name.isBlank()) {
            return patientRepository.findByFullNameContainingIgnoreCase(name.trim())
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    /**
     * List all patients with pagination (for reception search page "all patients below").
     * Default size 500 if not specified.
     */
    @Transactional(readOnly = true)
    public List<PatientResponseDto> list(int page, int size) {
        int safeSize = size <= 0 ? 500 : Math.min(size, 2000);
        Pageable pageable = PageRequest.of(Math.max(0, page), safeSize);
        return patientRepository.findAll(pageable).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PatientResponseDto toResponse(Patient p) {
        PatientResponseDto dto = new PatientResponseDto();
        dto.setId(p.getId());
        dto.setUhid(p.getUhid());
        dto.setRegistrationNumber(p.getRegistrationNumber());
        dto.setRegistrationDate(p.getRegistrationDate());
        dto.setFullName(p.getFullName());
        dto.setIdProofType(p.getIdProofType());
        dto.setIdProofNumber(p.getIdProofNumber());
        dto.setDateOfBirth(p.getDateOfBirth());
        dto.setAge(p.getAge());
        dto.setAgeYears(p.getAgeYears());
        dto.setAgeMonths(p.getAgeMonths());
        dto.setAgeDays(p.getAgeDays());
        dto.setGender(p.getGender());
        dto.setWeightKg(p.getWeightKg());
        dto.setHeightCm(p.getHeightCm());
        dto.setPhone(p.getPhone());
        dto.setAddress(p.getAddress());
        dto.setState(p.getState());
        dto.setCity(p.getCity());
        dto.setDistrict(p.getDistrict());
        dto.setFatherHusbandName(p.getFatherHusbandName());
        dto.setReferredBy(p.getReferredBy());
        dto.setReferredName(p.getReferredName());
        dto.setReferredPhone(p.getReferredPhone());
        dto.setConsultantName(p.getConsultantName());
        dto.setSpecialization(p.getSpecialization());
        dto.setOrganisationType(p.getOrganisationType());
        dto.setOrganisationName(p.getOrganisationName());
        dto.setRemarks(p.getRemarks());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }
}
