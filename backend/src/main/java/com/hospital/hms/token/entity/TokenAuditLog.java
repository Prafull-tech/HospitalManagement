package com.hospital.hms.token.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Audit log for token lifecycle events.
 */
@Entity
@Table(
    name = "token_audit_logs",
    indexes = {
        @Index(name = "idx_token_audit_token", columnList = "token_id"),
        @Index(name = "idx_token_audit_event", columnList = "event_type")
    }
)
public class TokenAuditLog extends BaseIdEntity {

    @Column(name = "token_id", nullable = false)
    private Long tokenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private TokenAuditEventType eventType;

    @Column(name = "performed_by", length = 255)
    private String performedBy;

    @Column(name = "event_at", nullable = false)
    private Instant eventAt;

    public TokenAuditLog() {
    }

    public Long getTokenId() { return tokenId; }
    public void setTokenId(Long tokenId) { this.tokenId = tokenId; }
    public TokenAuditEventType getEventType() { return eventType; }
    public void setEventType(TokenAuditEventType eventType) { this.eventType = eventType; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public Instant getEventAt() { return eventAt; }
    public void setEventAt(Instant eventAt) { this.eventAt = eventAt; }
}
