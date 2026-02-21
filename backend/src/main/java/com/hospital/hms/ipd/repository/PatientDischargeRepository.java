package com.hospital.hms.ipd.repository;

import com.hospital.hms.ipd.entity.PatientDischarge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientDischargeRepository extends JpaRepository<PatientDischarge, Long> {

    Optional<PatientDischarge> findByIpdAdmissionId(Long ipdAdmissionId);
}
