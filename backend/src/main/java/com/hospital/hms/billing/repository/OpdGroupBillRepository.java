package com.hospital.hms.billing.repository;

import com.hospital.hms.billing.entity.OpdGroupBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpdGroupBillRepository extends JpaRepository<OpdGroupBill, Long> {

    List<OpdGroupBill> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<OpdGroupBill> findByBillingAccountId(Long billingAccountId);
}
