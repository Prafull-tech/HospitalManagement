package com.hospital.hms.ward.controller;

import com.hospital.hms.ward.dto.RoomRequestDto;
import com.hospital.hms.ward.dto.RoomResponseDto;
import com.hospital.hms.ward.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Global room management APIs.
 * Base path: /api (context) + /rooms (mapping).
 */
@RestController
@RequestMapping("/rooms")
public class RoomManagementController {

    private final RoomService roomService;

    public RoomManagementController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','IPD_MANAGER','NURSING_SUPERINTENDENT','WARD_INCHARGE')")
    public ResponseEntity<RoomResponseDto> update(@PathVariable Long id,
                                                  @Valid @RequestBody RoomRequestDto request,
                                                  Authentication auth) {
        boolean isAdminOrIpdManager = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_IPD_MANAGER"));
        boolean isStatusOnlyRole = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_NURSING_SUPERINTENDENT") ||
                        a.getAuthority().equals("ROLE_WARD_INCHARGE"));
        RoomResponseDto updated = roomService.update(id, request, isAdminOrIpdManager, isStatusOnlyRole);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        roomService.disable(id);
        return ResponseEntity.noContent().build();
    }
}

