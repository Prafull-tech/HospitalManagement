package com.hospital.hms.tenant.service;

import com.hospital.hms.common.exception.OperationNotAllowedException;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.hospital.repository.HospitalRepository;
import com.hospital.hms.tenant.context.TenantContextHolder;
import com.hospital.hms.tenant.context.TenantRequestContext;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TenantContextService {

    private final HospitalRepository hospitalRepository;

    public TenantContextService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    public Optional<TenantRequestContext> getCurrentContext() {
        return TenantContextHolder.get();
    }

    public Optional<Long> getCurrentHospitalId() {
        return getCurrentContext()
                .filter(TenantRequestContext::hasHospital)
                .map(TenantRequestContext::getHospitalId);
    }

    public Long requireCurrentHospitalId() {
        return getCurrentHospitalId()
                .orElseThrow(() -> new OperationNotAllowedException("Hospital context is required for this operation"));
    }

    public Hospital requireCurrentHospital() {
        Long hospitalId = requireCurrentHospitalId();
        return hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found: " + hospitalId));
    }
}