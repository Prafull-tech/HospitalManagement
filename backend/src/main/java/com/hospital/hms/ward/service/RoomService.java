package com.hospital.hms.ward.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.ward.dto.RoomRequestDto;
import com.hospital.hms.ward.dto.RoomResponseDto;
import com.hospital.hms.ward.entity.Room;
import com.hospital.hms.ward.entity.Ward;
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

    public RoomService(RoomRepository roomRepository, WardRepository wardRepository) {
        this.roomRepository = roomRepository;
        this.wardRepository = wardRepository;
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
        room.setRoomNumber(request.getRoomNumber().trim());
        room.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        room = roomRepository.save(room);
        return toDto(room);
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

    private RoomResponseDto toDto(Room r) {
        RoomResponseDto dto = new RoomResponseDto();
        dto.setId(r.getId());
        dto.setWardId(r.getWard().getId());
        dto.setWardName(r.getWard().getName());
        dto.setRoomNumber(r.getRoomNumber());
        dto.setIsActive(r.getIsActive());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}
