package com.hospital.hms.appointment.repository;

import com.hospital.hms.appointment.entity.AppointmentAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentAuditLogRepository extends JpaRepository<AppointmentAuditLog, Long> {

    List<AppointmentAuditLog> findByAppointmentIdOrderByEventAtDesc(Long appointmentId);
}
