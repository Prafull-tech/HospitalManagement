package com.hospital.hms.nursing.repository;

import com.hospital.hms.nursing.entity.NurseRole;
import com.hospital.hms.nursing.entity.NursingStaff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Nursing staff repository. DB-agnostic.
 */
public interface NursingStaffRepository extends JpaRepository<NursingStaff, Long> {

    Optional<NursingStaff> findByCode(String code);

    List<NursingStaff> findByIsActiveTrueOrderByFullNameAsc();

    List<NursingStaff> findByIsActiveTrueAndNurseRoleOrderByFullNameAsc(NurseRole nurseRole);
}
