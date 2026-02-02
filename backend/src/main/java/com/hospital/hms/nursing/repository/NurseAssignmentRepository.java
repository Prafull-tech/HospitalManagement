package com.hospital.hms.nursing.repository;

import com.hospital.hms.nursing.entity.NurseAssignment;
import com.hospital.hms.nursing.entity.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Nurse assignment repository. DB-agnostic.
 */
public interface NurseAssignmentRepository extends JpaRepository<NurseAssignment, Long> {

    List<NurseAssignment> findByIpdAdmissionIdOrderByAssignmentDateDescShiftTypeAsc(Long ipdAdmissionId);

    List<NurseAssignment> findByNursingStaffIdOrderByAssignmentDateDesc(Long nursingStaffId);

    List<NurseAssignment> findByAssignmentDateAndShiftTypeOrderByAssignedAtAsc(LocalDate assignmentDate, ShiftType shiftType);
}
