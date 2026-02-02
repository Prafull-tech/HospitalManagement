package com.hospital.hms.ipd.repository;

import com.hospital.hms.ipd.entity.TransferRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository for transfer recommendations. DB-agnostic (H2 & MySQL).
 */
public interface TransferRecommendationRepository extends JpaRepository<TransferRecommendation, Long> {

    List<TransferRecommendation> findByIpdAdmissionIdOrderByRecommendationTimeDesc(Long ipdAdmissionId);

    /** Emergency transfers that still require written justification (misuse prevention). */
    @Query("SELECT r FROM TransferRecommendation r WHERE r.emergencyFlag = true AND r.emergencyJustification IS NULL ORDER BY r.recommendationTime DESC")
    List<TransferRecommendation> findEmergencyPendingJustification();
}
