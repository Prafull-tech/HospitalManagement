package com.hospital.hms.ipd.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.ipd.entity.TransferRecommendation;
import com.hospital.hms.ipd.repository.TransferRecommendationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Emergency transfer logic. Service only; no controller.
 * <ul>
 *   <li>Allow bypass of consent temporarily when emergency flag is set</li>
 *   <li>Require written justification later; track justification and who/when</li>
 *   <li>Flag emergency transfers clearly (emergencyFlag on recommendation)</li>
 *   <li>Prevent misuse: list pending justifications, validate justification before recording</li>
 * </ul>
 * DB-agnostic.
 */
@Service
public class EmergencyTransferService {

    private static final int MIN_JUSTIFICATION_LENGTH = 20;

    private final TransferRecommendationRepository transferRecommendationRepository;

    public EmergencyTransferService(TransferRecommendationRepository transferRecommendationRepository) {
        this.transferRecommendationRepository = transferRecommendationRepository;
    }

    /**
     * True when consent can be bypassed temporarily (emergency transfer).
     * Non-emergency transfers require consent before proceeding.
     */
    public boolean canBypassConsent(TransferRecommendation recommendation) {
        if (recommendation == null) {
            return false;
        }
        return Boolean.TRUE.equals(recommendation.getEmergencyFlag());
    }

    /**
     * True when this is an emergency transfer and written justification is not yet recorded.
     * Used to enforce "require written justification later" and prevent misuse.
     */
    public boolean requiresJustificationLater(TransferRecommendation recommendation) {
        if (recommendation == null) {
            return false;
        }
        return Boolean.TRUE.equals(recommendation.getEmergencyFlag())
                && (recommendation.getEmergencyJustification() == null
                || recommendation.getEmergencyJustification().isBlank());
    }

    /**
     * True when transfer is flagged as emergency. Use for clear flagging in UI/reports.
     */
    public boolean isEmergencyTransfer(TransferRecommendation recommendation) {
        return recommendation != null && Boolean.TRUE.equals(recommendation.getEmergencyFlag());
    }

    /**
     * Record written justification for an emergency transfer. Prevents misuse by requiring
     * non-blank justification and ensuring the recommendation is actually an emergency.
     *
     * @param recommendationId id of the transfer recommendation
     * @param justification    written justification (min length enforced)
     * @param justifiedBy      username or identifier of person recording justification
     * @throws IllegalArgumentException if not emergency, or justification blank/too short
     */
    @Transactional
    public void recordJustification(Long recommendationId, String justification, String justifiedBy) {
        TransferRecommendation rec = transferRecommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer recommendation not found: " + recommendationId));

        if (!Boolean.TRUE.equals(rec.getEmergencyFlag())) {
            throw new IllegalArgumentException("Justification applies only to emergency transfers. This recommendation is not marked as emergency.");
        }

        if (justification == null || justification.isBlank()) {
            throw new IllegalArgumentException("Written justification is required for emergency transfers. Provide a non-blank justification.");
        }
        String trimmed = justification.trim();
        if (trimmed.length() < MIN_JUSTIFICATION_LENGTH) {
            throw new IllegalArgumentException(
                    "Justification must be at least " + MIN_JUSTIFICATION_LENGTH + " characters. Prevents misuse.");
        }

        if (justifiedBy == null || justifiedBy.isBlank()) {
            throw new IllegalArgumentException("Justified-by (username or identifier) is required for audit.");
        }

        rec.setEmergencyJustification(trimmed);
        rec.setEmergencyJustificationAt(Instant.now());
        rec.setEmergencyJustificationBy(justifiedBy.trim());
        transferRecommendationRepository.save(rec);
    }

    /**
     * List emergency transfers that still require written justification. Use for audit and misuse prevention.
     */
    @Transactional(readOnly = true)
    public List<TransferRecommendation> listEmergencyPendingJustification() {
        return transferRecommendationRepository.findEmergencyPendingJustification();
    }

    /**
     * Returns true when written justification has been recorded for this emergency transfer.
     */
    public boolean hasJustificationRecorded(TransferRecommendation recommendation) {
        if (recommendation == null) {
            return false;
        }
        String j = recommendation.getEmergencyJustification();
        return j != null && !j.isBlank();
    }
}
