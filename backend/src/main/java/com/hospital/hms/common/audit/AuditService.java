package com.hospital.hms.common.audit;

import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.common.logging.SecurityContextUserResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Async
    public void record(String entityType, Long entityId, String action, String details) {
        try {
            AuditEvent event = new AuditEvent();
            event.setEntityType(entityType);
            event.setEntityId(entityId);
            event.setAction(action);
            event.setDetails(details);
            event.setUsername(SecurityContextUserResolver.resolveUserId());
            event.setCorrelationId(MDC.get(MdcKeys.CORRELATION_ID));
            repository.save(event);
        } catch (Exception ex) {
            log.error("Failed to record audit event: entityType={}, entityId={}, action={}: {}",
                    entityType, entityId, action, ex.getMessage());
        }
    }

    public void recordSync(String entityType, Long entityId, String action, String details) {
        AuditEvent event = new AuditEvent();
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setAction(action);
        event.setDetails(details);
        event.setUsername(SecurityContextUserResolver.resolveUserId());
        event.setCorrelationId(MDC.get(MdcKeys.CORRELATION_ID));
        repository.save(event);
    }
}
