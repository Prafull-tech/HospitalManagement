package com.hospital.hms.billing.repository;

import com.hospital.hms.billing.entity.BillStatus;
import com.hospital.hms.billing.entity.PatientBillingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface PatientBillingAccountRepository extends JpaRepository<PatientBillingAccount, Long> {

    Optional<PatientBillingAccount> findByIpdAdmissionId(Long ipdAdmissionId);

    Optional<PatientBillingAccount> findByOpdVisitId(Long opdVisitId);

    Optional<PatientBillingAccount> findByIpdAdmissionIdAndBillStatus(Long ipdAdmissionId, BillStatus status);

    @Query("SELECT COALESCE(SUM(a.pendingAmount), 0) FROM PatientBillingAccount a WHERE a.billStatus = 'ACTIVE'")
    BigDecimal sumPendingActiveAccounts();
}
