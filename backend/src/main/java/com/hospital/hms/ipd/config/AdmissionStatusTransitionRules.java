package com.hospital.hms.ipd.config;

import com.hospital.hms.ipd.entity.AdmissionStatus;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * IPD Admission Status Master: controlled status transitions.
 * All status changes must be validated against these rules.
 * Terminal statuses: DISCHARGED, CANCELLED, REFERRED, LAMA, EXPIRED (no outgoing transitions).
 * <p>
 * Master statuses (primary lifecycle): ACTIVE, DISCHARGED, SHIFTED (stored as TRANSFERRED),
 * REFERRED, LAMA, EXPIRED. Internal workflow also uses ADMITTED, DISCHARGE_INITIATED, CANCELLED.
 */
public final class AdmissionStatusTransitionRules {

    /** Master statuses for API/documentation: ACTIVE, DISCHARGED, SHIFTED/TRANSFERRED, REFERRED, LAMA, EXPIRED. */
    public static final Set<AdmissionStatus> MASTER_STATUSES = Set.of(
            AdmissionStatus.ACTIVE,
            AdmissionStatus.DISCHARGED,
            AdmissionStatus.TRANSFERRED,  // displayed as SHIFTED
            AdmissionStatus.REFERRED,
            AdmissionStatus.LAMA,
            AdmissionStatus.EXPIRED
    );

    /** Initial creation: no previous status → ADMITTED only. */
    public static final Set<AdmissionStatus> FROM_NULL = Set.of(AdmissionStatus.ADMITTED);

    /** Allowed target statuses from each source status. */
    private static final Map<AdmissionStatus, Set<AdmissionStatus>> ALLOWED_TRANSITIONS = Map.ofEntries(
            Map.entry(AdmissionStatus.ADMITTED, Set.of(
                    AdmissionStatus.ACTIVE,      // shift to ward
                    AdmissionStatus.CANCELLED
            )),
            Map.entry(AdmissionStatus.ACTIVE, Set.of(
                    AdmissionStatus.TRANSFERRED, // transfer / shift to another bed/ward
                    AdmissionStatus.DISCHARGE_INITIATED,
                    AdmissionStatus.REFERRED,
                    AdmissionStatus.LAMA,
                    AdmissionStatus.EXPIRED
            )),
            Map.entry(AdmissionStatus.TRANSFERRED, Set.of(
                    AdmissionStatus.ACTIVE,      // transfer back / downgrade
                    AdmissionStatus.DISCHARGE_INITIATED,
                    AdmissionStatus.REFERRED,
                    AdmissionStatus.LAMA,
                    AdmissionStatus.EXPIRED
            )),
            Map.entry(AdmissionStatus.DISCHARGE_INITIATED, Set.of(
                    AdmissionStatus.DISCHARGED
            )),
            Map.entry(AdmissionStatus.DISCHARGED, Collections.emptySet()),
            Map.entry(AdmissionStatus.CANCELLED, Collections.emptySet()),
            Map.entry(AdmissionStatus.REFERRED, Collections.emptySet()),
            Map.entry(AdmissionStatus.LAMA, Collections.emptySet()),
            Map.entry(AdmissionStatus.EXPIRED, Collections.emptySet())
    );

    /** Statuses that are considered "active" (patient still in care). */
    public static final Set<AdmissionStatus> ACTIVE_STATUSES = Set.of(
            AdmissionStatus.ADMITTED,
            AdmissionStatus.ACTIVE,
            AdmissionStatus.TRANSFERRED,
            AdmissionStatus.DISCHARGE_INITIATED
    );

    /** Terminal statuses: no further transitions allowed. */
    public static final Set<AdmissionStatus> TERMINAL_STATUSES = Set.of(
            AdmissionStatus.DISCHARGED,
            AdmissionStatus.CANCELLED,
            AdmissionStatus.REFERRED,
            AdmissionStatus.LAMA,
            AdmissionStatus.EXPIRED
    );

    private AdmissionStatusTransitionRules() {
    }

    /**
     * Returns allowed target statuses from the given source status.
     * For new admission use null as fromStatus (allowed target: ADMITTED only).
     */
    public static Set<AdmissionStatus> getAllowedTargets(AdmissionStatus fromStatus) {
        if (fromStatus == null) {
            return FROM_NULL;
        }
        return ALLOWED_TRANSITIONS.getOrDefault(fromStatus, Collections.emptySet());
    }

    /** Returns true if transition from -> to is allowed. */
    public static boolean isAllowed(AdmissionStatus fromStatus, AdmissionStatus toStatus) {
        if (toStatus == null) {
            return false;
        }
        Set<AdmissionStatus> allowed = fromStatus == null ? FROM_NULL : ALLOWED_TRANSITIONS.get(fromStatus);
        return allowed != null && allowed.contains(toStatus);
    }

    /** Returns true if the status is terminal (no outgoing transitions). */
    public static boolean isTerminal(AdmissionStatus status) {
        return TERMINAL_STATUSES.contains(status);
    }

    /** Returns all statuses in the master (for API listing). */
    public static Set<AdmissionStatus> getAllStatuses() {
        return EnumSet.allOf(AdmissionStatus.class);
    }

    /** Returns only the 6 primary master statuses: ACTIVE, DISCHARGED, TRANSFERRED, REFERRED, LAMA, EXPIRED. */
    public static Set<AdmissionStatus> getMasterStatusesOnly() {
        return MASTER_STATUSES;
    }
}
