package com.hospital.hms.lab.repository;

import com.hospital.hms.lab.entity.TestCategory;
import com.hospital.hms.lab.entity.TestMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TestMaster entity.
 */
@Repository
public interface TestMasterRepository extends JpaRepository<TestMaster, Long> {

    Optional<TestMaster> findByTestCodeIgnoreCase(String testCode);

    boolean existsByTestCodeIgnoreCase(String testCode);

    List<TestMaster> findByActiveTrueOrderByTestNameAsc();

    List<TestMaster> findByCategoryAndActiveTrue(TestCategory category);

    List<TestMaster> findByIsPanelTrueAndActiveTrue();

    List<TestMaster> findByPriorityLevelAndActiveTrue(com.hospital.hms.lab.entity.PriorityLevel priorityLevel);
}
