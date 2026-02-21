package com.hospital.hms.pharmacy.repository;

import com.hospital.hms.pharmacy.entity.MedicationOrder;
import com.hospital.hms.pharmacy.entity.MedicationOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicationOrderRepository extends JpaRepository<MedicationOrder, Long> {

    List<MedicationOrder> findByStatusOrderByOrderedAtAsc(MedicationOrderStatus status);

    @Query("SELECT m FROM MedicationOrder m WHERE m.status = :status ORDER BY " +
           "CASE WHEN m.priority = com.hospital.hms.pharmacy.entity.MedicationOrderPriority.HIGH THEN 0 ELSE 1 END, m.orderedAt ASC")
    List<MedicationOrder> findByStatusOrderByPriorityAndOrderedAt(
            @Param("status") MedicationOrderStatus status);

    List<MedicationOrder> findByIpdAdmissionIdAndStatus(Long ipdAdmissionId, MedicationOrderStatus status);

    List<MedicationOrder> findByOpdVisitIdAndStatus(Long opdVisitId, MedicationOrderStatus status);
}
