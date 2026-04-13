package com.hospital.hms.superadmin.repository;

import com.hospital.hms.superadmin.entity.HospitalModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HospitalModuleRepository extends JpaRepository<HospitalModule, Long> {

    List<HospitalModule> findByHospitalIdOrderByModuleCodeAsc(Long hospitalId);

    Optional<HospitalModule> findByHospitalIdAndModuleCode(Long hospitalId, String moduleCode);
}