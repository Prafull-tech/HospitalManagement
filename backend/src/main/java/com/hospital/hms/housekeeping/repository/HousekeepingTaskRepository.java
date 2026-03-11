package com.hospital.hms.housekeeping.repository;

import com.hospital.hms.housekeeping.entity.HousekeepingTask;
import com.hospital.hms.housekeeping.entity.HousekeepingTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HousekeepingTaskRepository extends JpaRepository<HousekeepingTask, Long> {

    List<HousekeepingTask> findByStatusOrderByCreatedAtAsc(HousekeepingTaskStatus status);

    List<HousekeepingTask> findAllByOrderByCreatedAtAsc();
}
