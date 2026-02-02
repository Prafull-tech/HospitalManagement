package com.hospital.hms.ipd.repository;

import com.hospital.hms.ipd.entity.SpecialAdmissionConsideration;
import com.hospital.hms.ipd.entity.SpecialConsiderationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Special admission consideration: type → priority boost.
 */
public interface SpecialAdmissionConsiderationRepository extends JpaRepository<SpecialAdmissionConsideration, Long> {

    Optional<SpecialAdmissionConsideration> findByConsiderationTypeAndActiveTrue(SpecialConsiderationType considerationType);

    List<SpecialAdmissionConsideration> findAllByActiveTrue();
}
