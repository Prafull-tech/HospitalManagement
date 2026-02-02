package com.hospital.hms.nursing.repository;

import com.hospital.hms.nursing.entity.NoteStatus;
import com.hospital.hms.nursing.entity.NursingNote;
import com.hospital.hms.nursing.entity.ShiftType;
import com.hospital.hms.ward.entity.WardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Nursing note repository. DB-agnostic. Search with denormalized fields for performance.
 */
public interface NursingNoteRepository extends JpaRepository<NursingNote, Long> {

    List<NursingNote> findByIpdAdmissionIdOrderByRecordedAtDesc(Long ipdAdmissionId);

    @Query("SELECT n FROM NursingNote n WHERE " +
           "(:patientName IS NULL OR LOWER(n.patientName) LIKE LOWER(CONCAT('%', :patientName, '%'))) " +
           "AND (:patientUhid IS NULL OR n.patientUhid = :patientUhid OR LOWER(n.patientUhid) LIKE LOWER(CONCAT('%', :patientUhid, '%'))) " +
           "AND (:bedNumber IS NULL OR LOWER(n.bedNumber) LIKE LOWER(CONCAT('%', :bedNumber, '%'))) " +
           "AND (:wardType IS NULL OR n.wardType = :wardType) " +
           "AND (:recordedDateFrom IS NULL OR n.recordedDate >= :recordedDateFrom) " +
           "AND (:recordedDateTo IS NULL OR n.recordedDate <= :recordedDateTo) " +
           "AND (:shiftType IS NULL OR n.shiftType = :shiftType) " +
           "AND (:noteStatus IS NULL OR n.noteStatus = :noteStatus)")
    Page<NursingNote> search(@Param("patientName") String patientName,
                             @Param("patientUhid") String patientUhid,
                             @Param("bedNumber") String bedNumber,
                             @Param("wardType") WardType wardType,
                             @Param("recordedDateFrom") LocalDate recordedDateFrom,
                             @Param("recordedDateTo") LocalDate recordedDateTo,
                             @Param("shiftType") ShiftType shiftType,
                             @Param("noteStatus") NoteStatus noteStatus,
                             Pageable pageable);

    /** Fuzzy search: q matches patient name (LIKE) OR UHID (exact or LIKE). Case-insensitive. */
    @Query("SELECT n FROM NursingNote n WHERE " +
           "(:q IS NULL OR :q = '' OR (LOWER(n.patientName) LIKE LOWER(CONCAT('%', :q, '%')) OR n.patientUhid = :q OR LOWER(n.patientUhid) LIKE LOWER(CONCAT('%', :q, '%')))) " +
           "AND (:bedNumber IS NULL OR :bedNumber = '' OR LOWER(n.bedNumber) LIKE LOWER(CONCAT('%', :bedNumber, '%'))) " +
           "AND (:wardType IS NULL OR n.wardType = :wardType) " +
           "AND (:recordedDateFrom IS NULL OR n.recordedDate >= :recordedDateFrom) " +
           "AND (:recordedDateTo IS NULL OR n.recordedDate <= :recordedDateTo) " +
           "AND (:shiftType IS NULL OR n.shiftType = :shiftType) " +
           "AND (:noteStatus IS NULL OR n.noteStatus = :noteStatus)")
    Page<NursingNote> searchWithQ(@Param("q") String q,
                                 @Param("bedNumber") String bedNumber,
                                 @Param("wardType") WardType wardType,
                                 @Param("recordedDateFrom") LocalDate recordedDateFrom,
                                 @Param("recordedDateTo") LocalDate recordedDateTo,
                                 @Param("shiftType") ShiftType shiftType,
                                 @Param("noteStatus") NoteStatus noteStatus,
                                 Pageable pageable);
}
