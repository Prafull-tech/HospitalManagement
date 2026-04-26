package com.hospital.hms.tenant.repository;

import com.hospital.hms.tenant.entity.TenantUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantUserRepository extends JpaRepository<TenantUser, String> {
    Optional<TenantUser> findByEmailIgnoreCase(String email);
}

