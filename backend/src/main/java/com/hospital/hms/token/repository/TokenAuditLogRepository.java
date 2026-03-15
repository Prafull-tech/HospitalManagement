package com.hospital.hms.token.repository;

import com.hospital.hms.token.entity.TokenAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenAuditLogRepository extends JpaRepository<TokenAuditLog, Long> {

    List<TokenAuditLog> findByTokenIdOrderByEventAtDesc(Long tokenId, org.springframework.data.domain.Pageable pageable);
}
