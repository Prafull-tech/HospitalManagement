package com.hospital.hms.doctor.repository;

import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.entity.DoctorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

/**
 * Doctor repository. JPQL only; no native SQL for DB-agnostic behaviour.
 */
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByCode(String code);

    List<Doctor> findByDepartmentId(Long departmentId);

    List<Doctor> findByStatus(DoctorStatus status);

    @EntityGraph(attributePaths = "department")
    @Query("SELECT d FROM Doctor d WHERE (:code IS NULL OR d.code = :code) " +
           "AND (:departmentId IS NULL OR d.department.id = :departmentId) " +
           "AND (:status IS NULL OR d.status = :status) " +
           "AND (:search IS NULL OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Doctor> search(@Param("code") String code,
                        @Param("departmentId") Long departmentId,
                        @Param("status") DoctorStatus status,
                        @Param("search") String search,
                        Pageable pageable);

    @Query("SELECT d FROM Doctor d JOIN FETCH d.department WHERE d.id = :id")
    Optional<Doctor> findByIdWithDepartment(@Param("id") Long id);
}
