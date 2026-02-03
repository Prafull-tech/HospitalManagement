package com.hospital.hms.billing.repository;

import com.hospital.hms.billing.entity.AdmissionCharge;
import com.hospital.hms.billing.entity.ChargeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdmissionChargeRepository extends JpaRepository<AdmissionCharge, Long> {

    List<AdmissionCharge> findByIpdAdmissionIdOrderByCreatedAtDesc(Long ipdAdmissionId);

    List<AdmissionCharge> findByIpdAdmissionIdAndChargeTypeOrderByCreatedAtDesc(Long ipdAdmissionId, ChargeType chargeType);
}
