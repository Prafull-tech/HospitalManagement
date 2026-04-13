package com.hospital.hms.lab.repository;

import com.hospital.hms.lab.entity.TATStatus;
import com.hospital.hms.lab.entity.TestOrder;
import com.hospital.hms.lab.entity.TestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TestOrder entity.
 */
@Repository
public interface TestOrderRepository extends JpaRepository<TestOrder, Long> {

    Optional<TestOrder> findByOrderNumber(String orderNumber);

    List<TestOrder> findByPatientIdOrderByOrderedAtDesc(Long patientId);

    List<TestOrder> findByIpdAdmissionIdOrderByOrderedAtDesc(Long ipdAdmissionId);

    List<TestOrder> findByOpdVisitIdOrderByOrderedAtDesc(Long opdVisitId);

    /** Emergency (isPriority) first, then by orderedAt. */
    @Query("SELECT t FROM TestOrder t WHERE t.status = :status ORDER BY t.isPriority DESC, t.orderedAt ASC")
    List<TestOrder> findByStatusOrderByIsPriorityDescOrderedAtAsc(@Param("status") TestStatus status);

    List<TestOrder> findByStatusAndIsPriorityTrueOrderByOrderedAtAsc(TestStatus status);

    /** Emergency first, then by sampleCollectedAt. */
    @Query("SELECT t FROM TestOrder t WHERE t.status = :status AND t.sampleCollectedAt IS NOT NULL ORDER BY t.isPriority DESC, t.sampleCollectedAt ASC")
    List<TestOrder> findPendingVerification(@Param("status") TestStatus status);

    @Query("SELECT t FROM TestOrder t WHERE t.status IN :statuses AND t.tatStatus = 'BREACH' ORDER BY t.tatEndTime DESC")
    List<TestOrder> findTATBreaches(@Param("statuses") List<TestStatus> statuses);

    @Query("SELECT t FROM TestOrder t WHERE t.status = :status AND t.isPriority = true ORDER BY t.orderedAt ASC")
    List<TestOrder> findEmergencySamplesPendingCollection(@Param("status") TestStatus status);

    @Query("SELECT t FROM TestOrder t WHERE t.sampleCollectedAt BETWEEN :startDate AND :endDate")
    List<TestOrder> findBySampleCollectionDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.status = :status AND t.orderedAt >= :startOfDay AND t.orderedAt < :endOfDay")
    Long countTodayByStatus(@Param("status") TestStatus status, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.status = :status AND t.releasedAt >= :startOfDay AND t.releasedAt < :endOfDay")
    Long countReleasedBetween(@Param("status") TestStatus status, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.orderedAt >= :startOfDay AND t.orderedAt < :endOfDay")
    Long countOrderedBetween(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.sampleCollectedAt >= :startOfDay AND t.sampleCollectedAt < :endOfDay")
    Long countCollectedBetween(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.resultEnteredAt >= :startOfDay AND t.resultEnteredAt < :endOfDay")
    Long countResultEnteredBetween(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.verifiedAt >= :startOfDay AND t.verifiedAt < :endOfDay")
    Long countVerifiedBetween(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.releasedAt >= :startOfDay AND t.releasedAt < :endOfDay AND t.tatStatus = :tatStatus")
    Long countReleasedWithTatStatusBetween(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay, @Param("tatStatus") TATStatus tatStatus);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.status = :status AND t.releasedAt >= :startOfDay AND t.releasedAt < :endOfDay AND t.isPriority = true")
    Long countReleasedBetweenWithPriority(@Param("status") TestStatus status, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    List<TestOrder> findByStatusIn(List<TestStatus> statuses);

    long countByStatus(TestStatus status);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.status = :status AND t.sampleCollectedAt IS NOT NULL")
    long countPendingVerification(@Param("status") TestStatus status);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.status IN :statuses AND t.tatStatus = 'BREACH'")
    long countTatBreaches(@Param("statuses") List<TestStatus> statuses);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.status = :status AND t.isPriority = true")
    long countEmergencySamplesPendingCollection(@Param("status") TestStatus status);

    // ─── Hospital-filtered variants (tenant isolation) ───

    @Query("SELECT t FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.status = :status ORDER BY t.isPriority DESC, t.orderedAt ASC")
    List<TestOrder> findByHospitalIdAndStatusOrderByIsPriorityDescOrderedAtAsc(@Param("hospitalId") Long hospitalId, @Param("status") TestStatus status);

    @Query("SELECT t FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.status = :status AND t.sampleCollectedAt IS NOT NULL ORDER BY t.isPriority DESC, t.sampleCollectedAt ASC")
    List<TestOrder> findPendingVerificationByHospitalId(@Param("hospitalId") Long hospitalId, @Param("status") TestStatus status);

    @Query("SELECT t FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.status IN :statuses AND t.tatStatus = 'BREACH' ORDER BY t.tatEndTime DESC")
    List<TestOrder> findTATBreachesByHospitalId(@Param("hospitalId") Long hospitalId, @Param("statuses") List<TestStatus> statuses);

    @Query("SELECT t FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.status = :status AND t.isPriority = true ORDER BY t.orderedAt ASC")
    List<TestOrder> findEmergencySamplesPendingCollectionByHospitalId(@Param("hospitalId") Long hospitalId, @Param("status") TestStatus status);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.status = :status AND t.orderedAt >= :startOfDay AND t.orderedAt < :endOfDay")
    Long countTodayByStatusAndHospitalId(@Param("hospitalId") Long hospitalId, @Param("status") TestStatus status, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.status = :status AND t.releasedAt >= :startOfDay AND t.releasedAt < :endOfDay")
    Long countReleasedBetweenByHospitalId(@Param("hospitalId") Long hospitalId, @Param("status") TestStatus status, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.orderedAt >= :startOfDay AND t.orderedAt < :endOfDay")
    Long countOrderedBetweenByHospitalId(@Param("hospitalId") Long hospitalId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.sampleCollectedAt >= :startOfDay AND t.sampleCollectedAt < :endOfDay")
    Long countCollectedBetweenByHospitalId(@Param("hospitalId") Long hospitalId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.resultEnteredAt >= :startOfDay AND t.resultEnteredAt < :endOfDay")
    Long countResultEnteredBetweenByHospitalId(@Param("hospitalId") Long hospitalId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.verifiedAt >= :startOfDay AND t.verifiedAt < :endOfDay")
    Long countVerifiedBetweenByHospitalId(@Param("hospitalId") Long hospitalId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.releasedAt >= :startOfDay AND t.releasedAt < :endOfDay AND t.tatStatus = :tatStatus")
    Long countReleasedWithTatStatusBetweenByHospitalId(@Param("hospitalId") Long hospitalId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay, @Param("tatStatus") TATStatus tatStatus);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.status = :status AND t.releasedAt >= :startOfDay AND t.releasedAt < :endOfDay AND t.isPriority = true")
    Long countReleasedBetweenWithPriorityByHospitalId(@Param("hospitalId") Long hospitalId, @Param("status") TestStatus status, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.status = :status")
    long countByStatusAndHospitalId(@Param("hospitalId") Long hospitalId, @Param("status") TestStatus status);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.status = :status AND t.sampleCollectedAt IS NOT NULL")
    long countPendingVerificationByHospitalId(@Param("hospitalId") Long hospitalId, @Param("status") TestStatus status);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.status IN :statuses AND t.tatStatus = 'BREACH'")
    long countTatBreachesByHospitalId(@Param("hospitalId") Long hospitalId, @Param("statuses") List<TestStatus> statuses);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.patient.hospital.id = :hospitalId AND t.status = :status AND t.isPriority = true")
    long countEmergencySamplesPendingCollectionByHospitalId(@Param("hospitalId") Long hospitalId, @Param("status") TestStatus status);
}
