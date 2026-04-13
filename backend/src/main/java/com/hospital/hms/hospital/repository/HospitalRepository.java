package com.hospital.hms.hospital.repository;

import com.hospital.hms.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for Hospital. DB-agnostic, no native queries.
 */
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    List<Hospital> findByDeletedFalseOrderByHospitalNameAsc();

    List<Hospital> findByDeletedFalseAndIsActiveTrueOrderByHospitalNameAsc();

    Optional<Hospital> findByHospitalCodeAndDeletedFalse(String hospitalCode);

    Optional<Hospital> findBySubdomainAndDeletedFalse(String subdomain);

    Optional<Hospital> findByCustomDomainAndDeletedFalse(String customDomain);

    boolean existsByHospitalCodeAndDeletedFalse(String hospitalCode);

    boolean existsBySubdomainAndDeletedFalse(String subdomain);

    boolean existsByCustomDomainAndDeletedFalse(String customDomain);

    boolean existsByHospitalCodeAndDeletedFalseAndIdNot(String hospitalCode, Long excludeId);

    boolean existsBySubdomainAndDeletedFalseAndIdNot(String subdomain, Long excludeId);

    boolean existsByCustomDomainAndDeletedFalseAndIdNot(String customDomain, Long excludeId);
}
