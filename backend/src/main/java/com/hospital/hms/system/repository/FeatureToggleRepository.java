package com.hospital.hms.system.repository;

import com.hospital.hms.system.entity.FeatureToggle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeatureToggleRepository extends JpaRepository<FeatureToggle, Long> {

    Optional<FeatureToggle> findByFeatureKey(String featureKey);

    List<FeatureToggle> findAllByOrderBySortOrderAscFeatureKeyAsc();
}
