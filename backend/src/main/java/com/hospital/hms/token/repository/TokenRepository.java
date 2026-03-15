package com.hospital.hms.token.repository;

import com.hospital.hms.token.entity.Token;
import com.hospital.hms.token.entity.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    List<Token> findByTokenDate(LocalDate tokenDate);

    @Query("""
        SELECT t FROM Token t
        JOIN FETCH t.patient p
        JOIN FETCH t.doctor d
        JOIN FETCH t.department
        WHERE t.doctor.id = :doctorId AND t.tokenDate = :tokenDate
        ORDER BY
          CASE t.priority
            WHEN 'EMERGENCY' THEN 1
            WHEN 'SENIOR' THEN 2
            WHEN 'PREGNANT' THEN 3
            WHEN 'FOLLOWUP' THEN 4
            ELSE 5
          END,
          COALESCE(t.skipSequence, 0),
          t.tokenNumber,
          t.id
        """)
    List<Token> findQueueByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("tokenDate") LocalDate tokenDate);

    @Query("""
        SELECT t FROM Token t
        JOIN FETCH t.patient p
        JOIN FETCH t.doctor d
        JOIN FETCH t.department
        WHERE t.doctor.id = :doctorId AND t.tokenDate = :tokenDate AND t.status = :status
        ORDER BY
          CASE t.priority
            WHEN 'EMERGENCY' THEN 1
            WHEN 'SENIOR' THEN 2
            WHEN 'PREGNANT' THEN 3
            WHEN 'FOLLOWUP' THEN 4
            ELSE 5
          END,
          COALESCE(t.skipSequence, 0),
          t.tokenNumber,
          t.id
        """)
    List<Token> findWaitingByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("tokenDate") LocalDate tokenDate, @Param("status") TokenStatus status);

    Optional<Token> findById(Long id);

    @Query("SELECT COALESCE(MAX(t.tokenNumber), 0) FROM Token t WHERE t.doctor.id = :doctorId AND t.tokenDate = :tokenDate")
    Integer findMaxTokenNumberForDoctorAndDate(@Param("doctorId") Long doctorId, @Param("tokenDate") LocalDate tokenDate);
}
