package com.hospital.hms.billing.repository;

import com.hospital.hms.billing.entity.Payment;
import com.hospital.hms.billing.entity.PatientBillingAccount;
import com.hospital.hms.reception.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByIpdAdmissionIdOrderByCreatedAtDesc(Long ipdAdmissionId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.createdAt >= :from AND p.createdAt <= :to")
    BigDecimal sumAmountByCreatedAtBetween(@Param("from") Instant from, @Param("to") Instant to);

    long countByCreatedAtBetween(Instant from, Instant to);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.createdAt >= :from AND p.createdAt <= :to " +
           "AND p.billingAccountId IN (SELECT ba.id FROM PatientBillingAccount ba WHERE ba.patientId IN " +
           "(SELECT pt.id FROM Patient pt WHERE pt.hospital.id = :hospitalId))")
    long countByHospitalIdAndCreatedAtBetween(@Param("hospitalId") Long hospitalId, @Param("from") Instant from, @Param("to") Instant to);

    Page<Payment> findByCreatedAtBetween(Instant from, Instant to, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :from AND p.createdAt <= :to " +
           "AND p.billingAccountId IN (SELECT ba.id FROM PatientBillingAccount ba WHERE ba.patientId IN " +
           "(SELECT pt.id FROM Patient pt WHERE pt.hospital.id = :hospitalId))")
    Page<Payment> findByHospitalIdAndCreatedAtBetween(@Param("hospitalId") Long hospitalId, @Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.createdAt >= :from AND p.createdAt <= :to " +
           "AND p.billingAccountId IN (SELECT ba.id FROM PatientBillingAccount ba WHERE ba.patientId IN " +
           "(SELECT pt.id FROM Patient pt WHERE pt.hospital.id = :hospitalId))")
    BigDecimal sumAmountByHospitalIdAndCreatedAtBetween(@Param("hospitalId") Long hospitalId, @Param("from") Instant from, @Param("to") Instant to);
}
