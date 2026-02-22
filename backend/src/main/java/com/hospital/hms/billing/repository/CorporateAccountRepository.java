package com.hospital.hms.billing.repository;

import com.hospital.hms.billing.entity.CorporateAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CorporateAccountRepository extends JpaRepository<CorporateAccount, Long> {

    Optional<CorporateAccount> findByCorporateCode(String corporateCode);
}
