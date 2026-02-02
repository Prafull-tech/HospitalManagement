package com.hospital.hms.ipd.service;

import com.hospital.hms.ipd.dto.AdmissionPriorityRequest;
import com.hospital.hms.ipd.dto.AdmissionPriorityResult;
import com.hospital.hms.ipd.entity.AdmissionConditionType;
import com.hospital.hms.ipd.entity.AdmissionPriorityRule;
import com.hospital.hms.ipd.entity.PriorityCode;
import com.hospital.hms.ipd.entity.SpecialAdmissionConsideration;
import com.hospital.hms.ipd.entity.SpecialConsiderationType;
import com.hospital.hms.ipd.repository.AdmissionPriorityRuleRepository;
import com.hospital.hms.ipd.repository.SpecialAdmissionConsiderationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Evaluates admission priority for a request.
 * <ul>
 *   <li>Resolves condition in order: Emergency → ICU → Referred → Elective</li>
 *   <li>Maps condition to base priority (P1–P4) via AdmissionPriorityRule</li>
 *   <li>Applies special consideration boost (never overrides P1 emergency; ICU never below P2)</li>
 *   <li>Returns final priority (P1–P4)</li>
 * </ul>
 * Service only; no controller.
 */
@Service
public class AdmissionPriorityEvaluationService {

    private static final Set<String> ICU_WARD_TYPES = Set.of("ICU", "CCU", "NICU", "HDU");

    private final AdmissionPriorityRuleRepository priorityRuleRepository;
    private final SpecialAdmissionConsiderationRepository specialConsiderationRepository;

    public AdmissionPriorityEvaluationService(AdmissionPriorityRuleRepository priorityRuleRepository,
                                              SpecialAdmissionConsiderationRepository specialConsiderationRepository) {
        this.priorityRuleRepository = priorityRuleRepository;
        this.specialConsiderationRepository = specialConsiderationRepository;
    }

    /**
     * Determines final admission priority (P1–P4) for the given request.
     * Emergency always P1; ICU never below P2; boost improves priority but does not override these.
     */
    @Transactional(readOnly = true)
    public PriorityCode evaluate(AdmissionPriorityRequest request) {
        return evaluateWithReason(request).getPriority();
    }

    /**
     * Evaluates priority and returns a human-readable reason for audit logging.
     */
    @Transactional(readOnly = true)
    public AdmissionPriorityResult evaluateWithReason(AdmissionPriorityRequest request) {
        AdmissionConditionType condition = resolveConditionType(request);
        PriorityCode basePriority = getBasePriority(condition);
        basePriority = enforceRules(condition, basePriority);

        int totalBoost = computeSpecialConsiderationBoost(request);
        int baseOrder = priorityToOrder(basePriority);
        int effectiveOrder = Math.max(1, baseOrder - totalBoost);
        if (condition == AdmissionConditionType.ICU && effectiveOrder > 2) {
            effectiveOrder = 2; // ICU never below P2
        }
        PriorityCode finalPriority = orderToPriority(effectiveOrder);
        String reason = buildAssignmentReason(condition, basePriority, totalBoost, finalPriority);
        List<String> appliedConsiderations = getAppliedSpecialConsiderationTypes(request);
        return new AdmissionPriorityResult(finalPriority, reason, condition, appliedConsiderations);
    }

    /** Returns list of special consideration type names that were applied (flag true and boost > 0). */
    private List<String> getAppliedSpecialConsiderationTypes(AdmissionPriorityRequest request) {
        List<String> applied = new ArrayList<>();
        if (request.isSeniorCitizen() && getBoostFor(SpecialConsiderationType.SENIOR_CITIZEN) > 0) {
            applied.add(SpecialConsiderationType.SENIOR_CITIZEN.name());
        }
        if (request.isPregnantWoman() && getBoostFor(SpecialConsiderationType.PREGNANT_WOMAN) > 0) {
            applied.add(SpecialConsiderationType.PREGNANT_WOMAN.name());
        }
        if (request.isChild() && getBoostFor(SpecialConsiderationType.CHILD) > 0) {
            applied.add(SpecialConsiderationType.CHILD.name());
        }
        if (request.isDisabledPatient() && getBoostFor(SpecialConsiderationType.DISABLED_PATIENT) > 0) {
            applied.add(SpecialConsiderationType.DISABLED_PATIENT.name());
        }
        return applied;
    }

    private static String buildAssignmentReason(AdmissionConditionType condition,
                                                PriorityCode basePriority, int totalBoost,
                                                PriorityCode finalPriority) {
        StringBuilder sb = new StringBuilder("Evaluated: ");
        sb.append(condition.name());
        if (totalBoost > 0) {
            sb.append(" + special consideration (boost ").append(totalBoost).append(")");
        }
        sb.append(" → ").append(finalPriority.name());
        return sb.toString();
    }

    /**
     * Resolve condition in order: 1. Emergency, 2. ICU, 3. Referred, 4. Elective.
     */
    AdmissionConditionType resolveConditionType(AdmissionPriorityRequest request) {
        if (isEmergency(request)) {
            return AdmissionConditionType.EMERGENCY;
        }
        if (isIcu(request)) {
            return AdmissionConditionType.ICU;
        }
        if (request.isReferred()) {
            return AdmissionConditionType.REFERRED;
        }
        return AdmissionConditionType.ELECTIVE;
    }

    private boolean isEmergency(AdmissionPriorityRequest request) {
        if (request.getAdmissionSource() != null
                && "EMERGENCY".equalsIgnoreCase(request.getAdmissionSource().trim())) {
            return true;
        }
        if (request.getWardType() != null
                && "EMERGENCY".equalsIgnoreCase(request.getWardType().trim())) {
            return true;
        }
        return false;
    }

    private boolean isIcu(AdmissionPriorityRequest request) {
        if (request.getWardType() == null) {
            return false;
        }
        String wt = request.getWardType().trim().toUpperCase();
        return ICU_WARD_TYPES.contains(wt);
    }

    private PriorityCode getBasePriority(AdmissionConditionType condition) {
        return priorityRuleRepository.findByConditionTypeAndActiveTrue(condition)
                .map(AdmissionPriorityRule::getMappedPriority)
                .orElseGet(() -> defaultPriorityFor(condition));
    }

    private static PriorityCode defaultPriorityFor(AdmissionConditionType condition) {
        return switch (condition) {
            case EMERGENCY -> PriorityCode.P1;
            case ICU -> PriorityCode.P2;
            case REFERRED -> PriorityCode.P3;
            case ELECTIVE -> PriorityCode.P4;
        };
    }

    /** Emergency always P1; ICU never below P2. */
    private static PriorityCode enforceRules(AdmissionConditionType condition, PriorityCode base) {
        if (condition == AdmissionConditionType.EMERGENCY) {
            return PriorityCode.P1;
        }
        if (condition == AdmissionConditionType.ICU && orderOf(base) > 2) {
            return PriorityCode.P2;
        }
        return base;
    }

    private int computeSpecialConsiderationBoost(AdmissionPriorityRequest request) {
        int total = 0;
        if (request.isSeniorCitizen()) {
            total += getBoostFor(SpecialConsiderationType.SENIOR_CITIZEN);
        }
        if (request.isPregnantWoman()) {
            total += getBoostFor(SpecialConsiderationType.PREGNANT_WOMAN);
        }
        if (request.isChild()) {
            total += getBoostFor(SpecialConsiderationType.CHILD);
        }
        if (request.isDisabledPatient()) {
            total += getBoostFor(SpecialConsiderationType.DISABLED_PATIENT);
        }
        return total;
    }

    private int getBoostFor(SpecialConsiderationType type) {
        return specialConsiderationRepository.findByConsiderationTypeAndActiveTrue(type)
                .map(SpecialAdmissionConsideration::getPriorityBoost)
                .orElse(0);
    }

    private static int priorityToOrder(PriorityCode p) {
        return switch (p) {
            case P1 -> 1;
            case P2 -> 2;
            case P3 -> 3;
            case P4 -> 4;
        };
    }

    private static int orderOf(PriorityCode p) {
        return priorityToOrder(p);
    }

    private static PriorityCode orderToPriority(int order) {
        return switch (order) {
            case 1 -> PriorityCode.P1;
            case 2 -> PriorityCode.P2;
            case 3 -> PriorityCode.P3;
            default -> PriorityCode.P4;
        };
    }
}
