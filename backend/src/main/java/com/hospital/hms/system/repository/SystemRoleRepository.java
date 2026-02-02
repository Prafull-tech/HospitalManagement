package com.hospital.hms.system.repository;

import com.hospital.hms.system.entity.SystemRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemRoleRepository extends JpaRepository<SystemRole, Long> {

    Optional<SystemRole> findByCode(String code);

    List<SystemRole> findAllByActiveTrueOrderBySortOrderAscCodeAsc();

    List<SystemRole> findAllByOrderBySortOrderAscCodeAsc();
}
