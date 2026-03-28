package com.hospital.hms.common.event;

import java.time.Instant;

/**
 * Base class for all HMS domain events.
 * Published via Spring's ApplicationEventPublisher for in-process async processing.
 */
public abstract class DomainEvent {

    private final Instant occurredAt = Instant.now();
    private final String triggeredBy;

    protected DomainEvent(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public Instant getOccurredAt() { return occurredAt; }
    public String getTriggeredBy() { return triggeredBy; }
}
