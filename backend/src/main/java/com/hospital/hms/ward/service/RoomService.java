package com.hospital.hms.ward.service;

import com.hospital.hms.common.exception.OperationNotAllowedException;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.ward.dto.RoomRequestDto;
import com.hospital.hms.ward.dto.RoomResponseDto;
import com.hospital.hms.ward.entity.BedStatus;
import com.hospital.hms.ward.entity.Room;
import com.hospital.hms.ward.entity.Ward;
import com.hospital.hms.ward.repository.BedRepository;
import com.hospital.hms.ward.repository.RoomRepository;
import com.hospital.hms.ward.repository.WardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Room service. Prevents duplicate room numbers in same ward. DB-agnostic.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final WardRepository wardRepository;
    private final BedRepository bedRepository;
    private final WardRoomAuditService auditService;

    public RoomService(RoomRepository roomRepository,
                       WardRepository wardRepository,
                       BedRepository bedRepository,
                       WardRoomAuditService auditService) {
        this.roomRepository = roomRepository;
        this.wardRepository = wardRepository;
        this.bedRepository = bedRepository;
        this.auditService = auditService;
    }

    @Transactional
    public RoomResponseDto create(Long wardId, RoomRequestDto request) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found: " + wardId));
        if (roomRepository.findByWardIdAndRoomNumber(wardId, request.getRoomNumber().trim()).isPresent()) {
            throw new IllegalArgumentException("Room number already exists in this ward: " + request.getRoomNumber());
        }
        Room room = new Room();
        room.setWard(ward);
        applyRequest(room, request, true, true);
        Room saved = roomRepository.save(room);
        RoomResponseDto dto = toDto(saved);
        auditService.log("ROOM", saved.getId(), "CREATE", null, dto);
        return dto;
    }

    public List<RoomResponseDto> listByWardId(Long wardId) {
        if (!wardRepository.existsById(wardId)) {
            throw new ResourceNotFoundException("Ward not found: " + wardId);
        }
        return roomRepository.findByWardIdAndIsActiveTrueOrderByRoomNumberAsc(wardId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomResponseDto update(Long id, RoomRequestDto request,
                                  boolean isAdminOrIpdManager,
                                  boolean isStatusOnlyRole) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));

        RoomResponseDto before = toDto(room);

        if (isStatusOnlyRole) {
            if (request.getStatus() != null) {
                room.setStatus(request.getStatus());
            }
        } else if (isAdminOrIpdManager) {
            applyRequest(room, request, true, true);
        } else {
            throw new OperationNotAllowedException("Not allowed to modify room");
        }

        Room saved = roomRepository.save(room);
        RoomResponseDto after = toDto(saved);
        auditService.log("ROOM", saved.getId(), "UPDATE", before, after);
        return after;
    }

    @Transactional
    public void disable(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));

        if (bedRepository.existsByRoom_IdAndBedStatusAndIsActiveTrue(id, BedStatus.OCCUPIED)) {
            throw new OperationNotAllowedException("Room cannot be disabled while beds are occupied");
        }
        if (Boolean.FALSE.equals(room.getIsActive())) {
            return;
        }

        RoomResponseDto before = toDto(room);
        room.setIsActive(false);
        Room saved = roomRepository.save(room);
        auditService.log("ROOM", saved.getId(), "DISABLE", before, toDto(saved));
    }

    private void applyRequest(Room room, RoomRequestDto request,
                              boolean allowStructuralChange,
                              boolean allowStatusChange) {
        room.setRoomNumber(request.getRoomNumber().trim());
        room.setCapacity(request.getCapacity());
        if (allowStructuralChange && request.getRoomType() != null) {
            room.setRoomType(request.getRoomType());
        }
        if (allowStatusChange && request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }
        if (request.getIsActive() != null) {
            room.setIsActive(request.getIsActive());
        }
    }

    private RoomResponseDto toDto(Room r) {
        RoomResponseDto dto = new RoomResponseDto();
        dto.setId(r.getId());
        dto.setWardId(r.getWard().getId());
        dto.setWardName(r.getWard().getName());
        dto.setRoomNumber(r.getRoomNumber());
        dto.setCapacity(r.getCapacity());
        dto.setRoomType(r.getRoomType());
        dto.setStatus(r.getStatus());
        dto.setIsActive(r.getIsActive());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}
