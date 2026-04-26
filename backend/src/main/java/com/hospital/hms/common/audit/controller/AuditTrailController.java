package com.hospital.hms.common.audit.controller;

import com.hospital.hms.common.audit.AuditEvent;
import com.hospital.hms.common.audit.AuditEventRepository;
import com.hospital.hms.common.audit.dto.AuditEventDto;
import com.hospital.hms.common.audit.dto.AuditEventPageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditTrailController {

    private final AuditEventRepository auditEventRepository;

    public AuditTrailController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @GetMapping
    public ResponseEntity<AuditEventPageResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(200, Math.max(1, size));
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AuditEvent> events = auditEventRepository.findAll(pageable);
        List<AuditEventDto> items = events.getContent().stream().map(this::toDto).toList();

        AuditEventPageResponse resp = new AuditEventPageResponse();
        resp.setItems(items);
        resp.setPage(events.getNumber());
        resp.setSize(events.getSize());
        resp.setTotalElements(events.getTotalElements());
        resp.setTotalPages(events.getTotalPages());
        return ResponseEntity.ok(resp);
    }

    private AuditEventDto toDto(AuditEvent e) {
        AuditEventDto dto = new AuditEventDto();
        dto.setId(e.getId());
        dto.setEntityType(e.getEntityType());
        dto.setEntityId(e.getEntityId());
        dto.setAction(e.getAction());
        dto.setUsername(e.getUsername());
        dto.setDetails(e.getDetails());
        dto.setIpAddress(e.getIpAddress());
        dto.setCorrelationId(e.getCorrelationId());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}

