package com.hospital.hms.billing.repository;

import com.hospital.hms.billing.entity.Payment;
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

    Page<Payment> findByCreatedAtBetween(Instant from, Instant to, Pageable pageable);
}
