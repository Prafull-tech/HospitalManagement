package com.hospital.hms.ipd.repository;

import com.hospital.hms.ipd.entity.TransferConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for transfer consent. DB-agnostic (H2 & MySQL).
 */
public interface TransferConsentRepository extends JpaRepository<TransferConsent, Long> {

    List<TransferConsent> findByTransferRecommendationIdOrderByCreatedAtDesc(Long transferRecommendationId);

    Optional<TransferConsent> findFirstByTransferRecommendationIdOrderByCreatedAtDesc(Long transferRecommendationId);
}
