package com.hospital.hms.pharmacy.repository;

import com.hospital.hms.pharmacy.entity.StockTransaction;
import com.hospital.hms.pharmacy.entity.StockTransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction> findByMedicine_IdOrderByPerformedAtDesc(Long medicineId, Pageable pageable);

    /** FEFO: earliest-expiring batches first for a medicine (PURCHASE only, expiry >= today). */
    @Query("SELECT s FROM StockTransaction s WHERE s.medicine.id = :medicineId " +
           "AND s.transactionType = com.hospital.hms.pharmacy.entity.StockTransactionType.PURCHASE " +
           "AND s.batchNumber IS NOT NULL AND (s.expiryDate IS NULL OR s.expiryDate >= :today) " +
           "ORDER BY s.expiryDate ASC")
    List<StockTransaction> findFefoBatchesForMedicine(
            @Param("medicineId") Long medicineId,
            @Param("today") LocalDate today,
            Pageable pageable);

    List<StockTransaction> findByTransactionTypeOrderByPerformedAtDesc(StockTransactionType type, Pageable pageable);

    List<StockTransaction> findByTransactionDateBetweenOrderByPerformedAtDesc(LocalDate from, LocalDate to, Pageable pageable);
}
