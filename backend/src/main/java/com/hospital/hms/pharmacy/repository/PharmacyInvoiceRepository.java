package com.hospital.hms.pharmacy.repository;

import com.hospital.hms.pharmacy.entity.PharmacyInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PharmacyInvoiceRepository extends JpaRepository<PharmacyInvoice, Long> {

    Optional<PharmacyInvoice> findByInvoiceNumber(String invoiceNumber);

    Optional<PharmacyInvoice> findBySale_Id(Long saleId);

    @Query("SELECT MAX(i.invoiceNumber) FROM PharmacyInvoice i WHERE i.invoiceNumber LIKE :prefix%")
    Optional<String> findMaxInvoiceNumberByPrefix(String prefix);
}
