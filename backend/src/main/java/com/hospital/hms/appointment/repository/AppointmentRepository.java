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

       List<Appointment> findByHospitalIdAndAppointmentDateAndDoctorIdOrderBySlotTimeAscTokenNoAsc(Long hospitalId, LocalDate date, Long doctorId);

    /** Check if an active (non-cancelled) appointment already exists for this doctor + date + slot. */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.hospital.id = :hospitalId AND a.doctor.id = :doctorId " +
           "AND a.appointmentDate = :date AND a.slotTime = :slotTime " +
           "AND a.status NOT IN ('CANCELLED')")
    boolean existsActiveByDoctorAndDateAndSlot(@Param("hospitalId") Long hospitalId,
                                               @Param("doctorId") Long doctorId,
                                               @Param("date") LocalDate date,
                                               @Param("slotTime") java.time.LocalTime slotTime);

    /** Same check but excluding a specific appointment (for reschedule). */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.hospital.id = :hospitalId AND a.doctor.id = :doctorId " +
           "AND a.appointmentDate = :date AND a.slotTime = :slotTime " +
           "AND a.status NOT IN ('CANCELLED') AND a.id <> :excludeId")
    boolean existsActiveByDoctorAndDateAndSlotExcluding(@Param("hospitalId") Long hospitalId,
                                                        @Param("doctorId") Long doctorId,
                                                        @Param("date") LocalDate date,
                                                        @Param("slotTime") java.time.LocalTime slotTime,
                                                        @Param("excludeId") Long excludeId);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.department " +
           "WHERE a.hospital.id = :hospitalId AND a.appointmentDate = :date AND a.doctor.id = :doctorId ORDER BY a.slotTime ASC, a.tokenNo ASC")
    List<Appointment> findQueueWithAssociations(@Param("hospitalId") Long hospitalId,
                                                @Param("date") LocalDate date,
                                                @Param("doctorId") Long doctorId);

    List<Appointment> findByHospitalIdAndAppointmentDateAndStatusInOrderBySlotTimeAsc(Long hospitalId, LocalDate date, List<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.department " +
           "WHERE a.hospital.id = :hospitalId AND a.appointmentDate = :date AND a.status IN :statuses ORDER BY a.slotTime ASC")
    List<Appointment> findByAppointmentDateAndStatusInWithAssociations(@Param("hospitalId") Long hospitalId,
                                                                       @Param("date") LocalDate date,
                                                                       @Param("statuses") List<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.department WHERE a.id = :id AND a.hospital.id = :hospitalId")
    Optional<Appointment> findByIdWithAssociations(@Param("id") Long id, @Param("hospitalId") Long hospitalId);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.department " +
           "WHERE a.hospital.id = :hospitalId " +
           "AND (:date IS NULL OR a.appointmentDate = :date) " +
           "AND (:doctorId IS NULL OR a.doctor.id = :doctorId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:patientUhid IS NULL OR LOWER(a.patient.uhid) LIKE LOWER(CONCAT('%', :patientUhid, '%'))) " +
           "AND (:patientName IS NULL OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :patientName, '%'))) " +
           "ORDER BY a.appointmentDate DESC, a.slotTime ASC")
    Page<Appointment> search(@Param("hospitalId") Long hospitalId,
                            @Param("date") LocalDate date,
                            @Param("doctorId") Long doctorId,
                            @Param("status") AppointmentStatus status,
                            @Param("patientUhid") String patientUhid,
                            @Param("patientName") String patientName,
                            Pageable pageable);

    long countByHospitalIdAndAppointmentDateAndStatus(Long hospitalId, LocalDate date, AppointmentStatus status);

    long countByHospitalIdAndAppointmentDateAndSource(Long hospitalId, LocalDate date, AppointmentSource source);
}
