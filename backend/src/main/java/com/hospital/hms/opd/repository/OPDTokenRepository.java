package com.hospital.hms.opd.repository;

import com.hospital.hms.opd.entity.OPDToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

/**
 * OPD token repository. Token number resets per doctor per day. DB-agnostic.
 */
public interface OPDTokenRepository extends JpaRepository<OPDToken, Long> {

    @Query("SELECT COALESCE(MAX(t.tokenNumber), 0) FROM OPDToken t WHERE t.doctor.id = :doctorId AND t.tokenDate = :tokenDate")
    Integer findMaxTokenNumberForDoctorAndDate(@Param("doctorId") Long doctorId, @Param("tokenDate") LocalDate tokenDate);

    Optional<OPDToken> findByVisitId(Long visitId);
}
