package com.hospital.hms.enquiry.repository;

import com.hospital.hms.enquiry.entity.Enquiry;
import com.hospital.hms.enquiry.entity.EnquiryCategory;
import com.hospital.hms.enquiry.entity.EnquiryPriority;
import com.hospital.hms.enquiry.entity.EnquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {

    @Query(
        value = """
            SELECT e FROM Enquiry e
            LEFT JOIN FETCH e.patient p
            LEFT JOIN FETCH e.department d
            WHERE (:status IS NULL OR e.status = :status)
              AND (:category IS NULL OR e.category = :category)
              AND (:priority IS NULL OR e.priority = :priority)
              AND (:departmentId IS NULL OR d.id = :departmentId)
              AND (:assignedToUser IS NULL OR LOWER(e.assignedToUser) = LOWER(:assignedToUser))
              AND (:createdFrom IS NULL OR e.createdAt >= :createdFrom)
              AND (:createdTo IS NULL OR e.createdAt < :createdTo)
              AND (:patientUhid IS NULL OR LOWER(p.uhid) LIKE LOWER(CONCAT('%', :patientUhid, '%')))
              AND (:query IS NULL OR LOWER(e.subject) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(e.enquirerName) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(e.phone) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :query, '%')))
            ORDER BY e.createdAt DESC
            """,
        countQuery = """
            SELECT COUNT(e) FROM Enquiry e
            LEFT JOIN e.patient p
            LEFT JOIN e.department d
            WHERE (:status IS NULL OR e.status = :status)
              AND (:category IS NULL OR e.category = :category)
              AND (:priority IS NULL OR e.priority = :priority)
              AND (:departmentId IS NULL OR d.id = :departmentId)
              AND (:assignedToUser IS NULL OR LOWER(e.assignedToUser) = LOWER(:assignedToUser))
              AND (:createdFrom IS NULL OR e.createdAt >= :createdFrom)
              AND (:createdTo IS NULL OR e.createdAt < :createdTo)
              AND (:patientUhid IS NULL OR LOWER(p.uhid) LIKE LOWER(CONCAT('%', :patientUhid, '%')))
              AND (:query IS NULL OR LOWER(e.subject) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(e.enquirerName) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(e.phone) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<Enquiry> search(@Param("status") EnquiryStatus status,
                         @Param("category") EnquiryCategory category,
                         @Param("priority") EnquiryPriority priority,
                         @Param("departmentId") Long departmentId,
                         @Param("assignedToUser") String assignedToUser,
                         @Param("createdFrom") Instant createdFrom,
                         @Param("createdTo") Instant createdTo,
                         @Param("patientUhid") String patientUhid,
                         @Param("query") String query,
                         Pageable pageable);

    @Query("SELECT e FROM Enquiry e LEFT JOIN FETCH e.patient LEFT JOIN FETCH e.department WHERE e.id = :id")
    Optional<Enquiry> findByIdWithAssociations(@Param("id") Long id);

    long countByStatus(EnquiryStatus status);

    List<Enquiry> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT e.category, COUNT(e) FROM Enquiry e GROUP BY e.category")
    List<Object[]> countByCategory();
}
