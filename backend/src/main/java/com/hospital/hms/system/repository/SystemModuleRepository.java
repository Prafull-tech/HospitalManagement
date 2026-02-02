package com.hospital.hms.system.repository;

import com.hospital.hms.system.entity.ModuleCategory;
import com.hospital.hms.system.entity.SystemModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemModuleRepository extends JpaRepository<SystemModule, Long> {

    Optional<SystemModule> findByCode(String code);

    List<SystemModule> findAllByEnabledTrueOrderByModuleCategoryAscSortOrderAscCodeAsc();

    List<SystemModule> findAllByOrderByModuleCategoryAscSortOrderAscCodeAsc();
}
