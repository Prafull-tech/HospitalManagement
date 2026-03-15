package com.hospital.hms.appointment.repository;

import com.hospital.hms.appointment.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    @Query("SELECT s FROM DoctorSchedule s JOIN FETCH s.doctor WHERE s.doctor.id = :doctorId ORDER BY s.dayOfWeek ASC, s.startTime ASC")
    List<DoctorSchedule> findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(@Param("doctorId") Long doctorId);

    void deleteByDoctorId(Long doctorId);
}
