package com.hospital.hms.appointment.repository;

import com.hospital.hms.appointment.entity.Appointment;
import com.hospital.hms.appointment.entity.AppointmentStatus;
import com.hospital.hms.appointment.entity.AppointmentSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByAppointmentDateAndDoctorIdOrderBySlotTimeAscTokenNoAsc(LocalDate date, Long doctorId);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.department " +
           "WHERE a.appointmentDate = :date AND a.doctor.id = :doctorId ORDER BY a.slotTime ASC, a.tokenNo ASC")
    List<Appointment> findQueueWithAssociations(@Param("date") LocalDate date, @Param("doctorId") Long doctorId);

    List<Appointment> findByAppointmentDateAndStatusInOrderBySlotTimeAsc(LocalDate date, List<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.department " +
           "WHERE a.appointmentDate = :date AND a.status IN :statuses ORDER BY a.slotTime ASC")
    List<Appointment> findByAppointmentDateAndStatusInWithAssociations(@Param("date") LocalDate date,
                                                                       @Param("statuses") List<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.department WHERE a.id = :id")
    Optional<Appointment> findByIdWithAssociations(@Param("id") Long id);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.department " +
           "WHERE (:date IS NULL OR a.appointmentDate = :date) " +
           "AND (:doctorId IS NULL OR a.doctor.id = :doctorId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:patientUhid IS NULL OR LOWER(a.patient.uhid) LIKE LOWER(CONCAT('%', :patientUhid, '%'))) " +
           "AND (:patientName IS NULL OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :patientName, '%'))) " +
           "ORDER BY a.appointmentDate DESC, a.slotTime ASC")
    Page<Appointment> search(@Param("date") LocalDate date,
                            @Param("doctorId") Long doctorId,
                            @Param("status") AppointmentStatus status,
                            @Param("patientUhid") String patientUhid,
                            @Param("patientName") String patientName,
                            Pageable pageable);

    long countByAppointmentDateAndStatus(LocalDate date, AppointmentStatus status);

    long countByAppointmentDateAndSource(LocalDate date, AppointmentSource source);
}
