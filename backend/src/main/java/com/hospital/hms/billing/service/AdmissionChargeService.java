package com.hospital.hms.billing.service;

import com.hospital.hms.billing.dto.AdmissionChargeRequestDto;
import com.hospital.hms.billing.entity.AdmissionCharge;
import com.hospital.hms.billing.entity.ChargeType;
import com.hospital.hms.billing.repository.AdmissionChargeRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Charges auto-added to billing by Pharmacy, Lab, Doctor Orders, etc. All linked with IPD Admission Number.
 */
@Service
public class AdmissionChargeService {

    private final AdmissionChargeRepository chargeRepository;
    private final IPDAdmissionRepository admissionRepository;

    public AdmissionChargeService(AdmissionChargeRepository chargeRepository, IPDAdmissionRepository admissionRepository) {
        this.chargeRepository = chargeRepository;
        this.admissionRepository = admissionRepository;
    }

    /**
     * Add a charge line for an IPD admission. Call this when Pharmacy dispenses, Lab completes, Order is executed, etc.
     */
    @Transactional
    public AdmissionCharge addCharge(Long ipdAdmissionId, AdmissionChargeRequestDto request) {
        var admission = admissionRepository.findById(ipdAdmissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId));
        AdmissionCharge charge = new AdmissionCharge();
        charge.setIpdAdmission(admission);
        charge.setChargeType(request.getChargeType());
        charge.setAmount(request.getAmount());
        charge.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        charge.setReferenceType(request.getReferenceType() != null ? request.getReferenceType().trim() : null);
        charge.setReferenceId(request.getReferenceId());
        return chargeRepository.save(charge);
    }

    @Transactional(readOnly = true)
    public List<AdmissionCharge> findByIpdAdmissionId(Long ipdAdmissionId) {
        if (!admissionRepository.existsById(ipdAdmissionId)) {
            throw new ResourceNotFoundException("IPD admission not found: " + ipdAdmissionId);
        }
        return chargeRepository.findByIpdAdmissionIdOrderByCreatedAtDesc(ipdAdmissionId);
    }
}
