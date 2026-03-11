package com.hospital.hms.lab.repository;

import com.hospital.hms.lab.entity.LabOrderItem;
import com.hospital.hms.lab.entity.TestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabOrderItemRepository extends JpaRepository<LabOrderItem, Long> {

    List<LabOrderItem> findByOrderIdOrderByIdAsc(Long orderId);

    /** LabOrderItems whose linked TestOrder is COLLECTED or IN_PROGRESS. Emergency (isPriority) first. */
    @Query("SELECT i FROM LabOrderItem i JOIN FETCH i.testOrder t JOIN FETCH t.patient JOIN FETCH i.testMaster JOIN FETCH i.order o " +
            "WHERE t.status IN :statuses ORDER BY t.isPriority DESC, i.id ASC")
    List<LabOrderItem> findByTestOrderStatusInOrderByIdAsc(@Param("statuses") java.util.List<TestStatus> statuses);

    /** LabOrderItems whose linked TestOrder is COMPLETED (awaiting verification). Emergency first. */
    @Query("SELECT i FROM LabOrderItem i JOIN FETCH i.testOrder t JOIN FETCH t.patient JOIN FETCH i.testMaster JOIN FETCH i.order o " +
            "WHERE t.status = :status ORDER BY t.isPriority DESC, i.id ASC")
    List<LabOrderItem> findByTestOrderStatusOrderByIdAsc(@Param("status") TestStatus status);
}
