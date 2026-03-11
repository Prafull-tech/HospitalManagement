package com.hospital.hms.lab.repository;

import com.hospital.hms.lab.entity.LabOrder;
import com.hospital.hms.lab.entity.LabOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {

    List<LabOrder> findByPatientIdOrderByOrderedAtDesc(Long patientId);

    List<LabOrder> findByIpdAdmission_IdOrderByOrderedAtDesc(Long ipdAdmissionId);

    List<LabOrder> findByOpdVisit_IdOrderByOrderedAtDesc(Long opdVisitId);

    List<LabOrder> findByStatusOrderByOrderedAtDesc(LabOrderStatus status);

    List<LabOrder> findAllByOrderByOrderedAtDesc();
}
