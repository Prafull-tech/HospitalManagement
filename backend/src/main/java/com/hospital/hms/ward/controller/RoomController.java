package com.hospital.hms.ward.controller;

import com.hospital.hms.ward.dto.RoomRequestDto;
import com.hospital.hms.ward.dto.RoomResponseDto;
import com.hospital.hms.ward.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for rooms under a ward. Base path: /api (context) + /wards (mapping).
 * Auth disabled; re-enable @PreAuthorize when auth is on.
 */
@RestController
@RequestMapping("/wards")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/{wardId}/rooms")
    public ResponseEntity<RoomResponseDto> create(@PathVariable Long wardId,
                                                   @Valid @RequestBody RoomRequestDto request) {
        RoomResponseDto created = roomService.create(wardId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{wardId}/rooms")
    public ResponseEntity<List<RoomResponseDto>> listByWardId(@PathVariable Long wardId) {
        List<RoomResponseDto> list = roomService.listByWardId(wardId);
        return ResponseEntity.ok(list);
    }
}
