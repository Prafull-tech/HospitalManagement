package com.hospital.hms.housekeeping.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.housekeeping.dto.HousekeepingTaskRequestDto;
import com.hospital.hms.housekeeping.dto.HousekeepingTaskResponseDto;
import com.hospital.hms.housekeeping.entity.HousekeepingTask;
import com.hospital.hms.housekeeping.entity.HousekeepingTaskStatus;
import com.hospital.hms.housekeeping.repository.HousekeepingTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Housekeeping task management: list, create, complete.
 */
@Service
public class HousekeepingTaskService {

    private static final Logger log = LoggerFactory.getLogger(HousekeepingTaskService.class);

    private final HousekeepingTaskRepository taskRepository;

    public HousekeepingTaskService(HousekeepingTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<HousekeepingTaskResponseDto> listTasks(HousekeepingTaskStatus status) {
        List<HousekeepingTask> tasks = status != null
                ? taskRepository.findByStatusOrderByCreatedAtAsc(status)
                : taskRepository.findAllByOrderByCreatedAtAsc();
        return tasks.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public HousekeepingTaskResponseDto createTask(HousekeepingTaskRequestDto request) {
        HousekeepingTask task = new HousekeepingTask();
        task.setBedId(request.getBedId());
        task.setRoomNo(request.getRoomNo());
        task.setWardName(request.getWardName());
        task.setTaskType(request.getTaskType());
        task.setAssignedStaff(request.getAssignedStaff());
        task.setIpdAdmissionId(request.getIpdAdmissionId());
        task.setStatus(HousekeepingTaskStatus.PENDING);

        task = taskRepository.save(task);
        String user = SecurityContextUserResolver.resolveUserId();
        log.info("Housekeeping task created id={} type={} by {}", task.getId(), task.getTaskType(), user);
        return toDto(task);
    }

    @Transactional
    public HousekeepingTaskResponseDto completeTask(Long id) {
        HousekeepingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Housekeeping task not found: " + id));

        task.setStatus(HousekeepingTaskStatus.COMPLETED);
        task.setCompletedAt(java.time.Instant.now());
        task = taskRepository.save(task);

        String user = SecurityContextUserResolver.resolveUserId();
        log.info("Housekeeping task completed id={} by {}", id, user);
        return toDto(task);
    }

    private HousekeepingTaskResponseDto toDto(HousekeepingTask task) {
        HousekeepingTaskResponseDto dto = new HousekeepingTaskResponseDto();
        dto.setId(task.getId());
        dto.setBedId(task.getBedId());
        dto.setRoomNo(task.getRoomNo());
        dto.setWardName(task.getWardName());
        dto.setTaskType(task.getTaskType());
        dto.setAssignedStaff(task.getAssignedStaff());
        dto.setStatus(task.getStatus());
        dto.setIpdAdmissionId(task.getIpdAdmissionId());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setCompletedAt(task.getCompletedAt());
        return dto;
    }
}
