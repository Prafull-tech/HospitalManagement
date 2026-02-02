package com.hospital.hms.doctor.repository;

import com.hospital.hms.doctor.entity.MedicalDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Department repository. DB-agnostic (derived queries only).
 */
public interface MedicalDepartmentRepository extends JpaRepository<MedicalDepartment, Long> {

    Optional<MedicalDepartment> findByCode(String code);

    List<MedicalDepartment> findAllByOrderByNameAsc();
}
