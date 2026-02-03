package com.hospital.hms.ward.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.ward.entity.WardRoomAuditLog;
import com.hospital.hms.ward.repository.WardRoomAuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Centralized audit logging for Ward & Room master changes.
 * Writes only; read APIs use WardRoomAuditLogRepository directly.
 */
@Service
public class WardRoomAuditService {

    private final WardRoomAuditLogRepository repository;
    private final ObjectMapper objectMapper;

    public WardRoomAuditService(WardRoomAuditLogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void log(String entityType, Long entityId, String action, Object oldValue, Object newValue) {
        WardRoomAuditLog log = new WardRoomAuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setPerformedAt(Instant.now());
        log.setPerformedBy(SecurityContextUserResolver.resolveUserId());
        log.setOldValue(toJson(oldValue));
        log.setNewValue(toJson(newValue));
        repository.save(log);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"failed to serialize\"}";
        }
    }
}

