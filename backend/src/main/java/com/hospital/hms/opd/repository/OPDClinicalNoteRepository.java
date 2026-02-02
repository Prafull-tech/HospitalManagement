package com.hospital.hms.opd.repository;

import com.hospital.hms.opd.entity.OPDClinicalNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * OPD clinical notes repository. DB-agnostic.
 */
public interface OPDClinicalNoteRepository extends JpaRepository<OPDClinicalNote, Long> {

    Optional<OPDClinicalNote> findByVisitId(Long visitId);
}
