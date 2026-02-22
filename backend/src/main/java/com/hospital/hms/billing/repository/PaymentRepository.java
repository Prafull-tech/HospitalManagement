package com.hospital.hms.billing.repository;

import com.hospital.hms.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByIpdAdmissionIdOrderByCreatedAtDesc(Long ipdAdmissionId);
}
