package com.hospital.hms.billing.repository;

import com.hospital.hms.billing.entity.BillingItem;
import com.hospital.hms.billing.entity.BillingServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillingItemRepository extends JpaRepository<BillingItem, Long> {

    List<BillingItem> findByBillingAccountIdOrderByCreatedAtDesc(Long billingAccountId);

    @Query("SELECT bi FROM BillingItem bi WHERE bi.billingAccount.id = :accountId AND bi.status = 'POSTED' ORDER BY bi.createdAt DESC")
    List<BillingItem> findPostedByBillingAccountId(@Param("accountId") Long billingAccountId);

    @Query("SELECT bi FROM BillingItem bi WHERE bi.billingAccount.id = :accountId AND bi.serviceType = :serviceType AND bi.status = 'POSTED' ORDER BY bi.createdAt DESC")
    List<BillingItem> findPostedByBillingAccountIdAndServiceType(
            @Param("accountId") Long billingAccountId,
            @Param("serviceType") BillingServiceType serviceType);

    @Query("SELECT bi FROM BillingItem bi WHERE bi.billingAccount.id = :accountId AND bi.serviceType = 'BED' AND bi.chargeDate = :chargeDate")
    Optional<BillingItem> findBedChargeByAccountAndDate(@Param("accountId") Long billingAccountId, @Param("chargeDate") LocalDate chargeDate);
}
