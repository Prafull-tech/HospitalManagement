package com.hospital.hms.ipd.service;

import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.entity.PriorityCode;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Admission queue sorting. Service logic only.
 * <ul>
 *   <li>Emergency cases (P1) always on top</li>
 *   <li>Sort by priority order (P1, P2, P3, P4; null treated as lowest)</li>
 *   <li>Then by arrival time (earlier first)</li>
 * </ul>
 * DB-agnostic.
 */
@Service
public class AdmissionQueueService {

    private static final List<AdmissionStatus> ACTIVE_STATUSES = List.of(
            AdmissionStatus.ADMITTED,
            AdmissionStatus.ACTIVE,
            AdmissionStatus.TRANSFERRED,
            AdmissionStatus.DISCHARGE_INITIATED
    );

    /** Priority order for queue: 1 = highest (P1/emergency), 4 = lowest (P4). Null = last. */
    private static final int NULL_PRIORITY_ORDER = 5;

    private final IPDAdmissionRepository admissionRepository;

    public AdmissionQueueService(IPDAdmissionRepository admissionRepository) {
        this.admissionRepository = admissionRepository;
    }

    /**
     * Returns active admissions in queue order: priority (P1 first), then arrival time ascending.
     * Emergency cases (P1) are always on top; within same priority, earlier arrival first.
     */
    @Transactional(readOnly = true)
    public List<IPDAdmission> getActiveAdmissionsInQueueOrder() {
        List<IPDAdmission> list = admissionRepository.findByAdmissionStatusIn(ACTIVE_STATUSES);
        return sortByQueueOrder(list);
    }

    /**
     * Sorts admissions by queue rules: priority order (P1 first), then arrival time ascending.
     * Does not modify the input list; returns a new sorted list.
     */
    public List<IPDAdmission> sortByQueueOrder(List<IPDAdmission> admissions) {
        if (admissions == null || admissions.isEmpty()) {
            return List.of();
        }
        List<IPDAdmission> copy = new ArrayList<>(admissions);
        copy.sort(queueComparator());
        return copy;
    }

    /**
     * Comparator: first by priority order (P1=1, P2=2, P3=3, P4=4, null=last), then by admissionDateTime ascending.
     */
    public static Comparator<IPDAdmission> queueComparator() {
        return Comparator
                .comparing(AdmissionQueueService::priorityOrderForQueue)
                .thenComparing(IPDAdmission::getAdmissionDateTime, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private static int priorityOrderForQueue(IPDAdmission a) {
        PriorityCode p = a.getAdmissionPriority();
        if (p == null) {
            return NULL_PRIORITY_ORDER;
        }
        return p.ordinal() + 1; // P1=1, P2=2, P3=3, P4=4
    }
}
