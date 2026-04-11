package com.hospital.hms.enquiry.repository;

import com.hospital.hms.enquiry.entity.EnquiryAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnquiryAuditLogRepository extends JpaRepository<EnquiryAuditLog, Long> {
    List<EnquiryAuditLog> findByEnquiryIdOrderByEventAtDesc(Long enquiryId);
}
