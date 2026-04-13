package com.hospital.hms.superadmin.repository;

import com.hospital.hms.superadmin.entity.HospitalSubscription;
import com.hospital.hms.superadmin.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HospitalSubscriptionRepository extends JpaRepository<HospitalSubscription, Long> {

    List<HospitalSubscription> findAllByOrderByStartDateDesc();

    List<HospitalSubscription> findByStatusOrderByStartDateDesc(SubscriptionStatus status);

    Optional<HospitalSubscription> findByHospitalIdAndStatusIn(Long hospitalId, List<SubscriptionStatus> statuses);

    List<HospitalSubscription> findByHospitalIdAndStatusInOrderByStartDateDescIdDesc(Long hospitalId, List<SubscriptionStatus> statuses);

    List<HospitalSubscription> findByHospitalIdOrderByStartDateDesc(Long hospitalId);

    long countByStatus(SubscriptionStatus status);
}
