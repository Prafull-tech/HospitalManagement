package com.hospital.hms.ward.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.ward.dto.BedResponseDto;
import com.hospital.hms.ward.dto.BedStatusRequestDto;
import com.hospital.hms.ward.entity.Bed;
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
 * Bed service. Prevents duplicate bed numbers in same ward. Only AVAILABLE beds can be allocated (enforced in IPD). DB-agnostic.
 */
@Service
public class BedService {

    private final BedRepository bedRepository;
    private final WardRepository wardRepository;
    private final RoomRepository roomRepository;

    public BedService(BedRepository bedRepository, WardRepository wardRepository, RoomRepository roomRepository) {
        this.bedRepository = bedRepository;
        this.wardRepository = wardRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public BedResponseDto create(Long wardId, com.hospital.hms.ward.dto.BedRequestDto request) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found: " + wardId));
        if (bedRepository.findByWardIdAndBedNumber(wardId, request.getBedNumber().trim()).isPresent()) {
            throw new IllegalArgumentException("Bed number already exists in this ward: " + request.getBedNumber());
        }
        Bed bed = new Bed();
        bed.setWard(ward);
        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + request.getRoomId()));
            if (!room.getWard().getId().equals(wardId)) {
                throw new IllegalArgumentException("Room does not belong to this ward.");
            }
            bed.setRoom(room);
        }
        bed.setBedNumber(request.getBedNumber().trim());
        bed.setBedStatus(request.getBedStatus() != null ? request.getBedStatus() : BedStatus.AVAILABLE);
        bed.setIsIsolation(request.getIsIsolation() != null ? request.getIsIsolation() : false);
        bed.setEquipmentReady(request.getEquipmentReady() != null ? request.getEquipmentReady() : true);
        bed.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        bed = bedRepository.save(bed);
        return toDto(bed);
    }

    @Transactional(readOnly = true)
    public List<BedResponseDto> listByWardId(Long wardId) {
        if (!wardRepository.existsById(wardId)) {
            throw new ResourceNotFoundException("Ward not found: " + wardId);
        }
        return bedRepository.findByWardIdWithWardAndRoom(wardId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * All beds with availability flag. Available = bedStatus == AVAILABLE and isActive. Ordered by ward name, bed number.
     */
    @Transactional(readOnly = true)
    public List<BedResponseDto> getAvailability(Long wardId) {
        List<Bed> beds = wardId != null
                ? bedRepository.findByWardIdWithWardAndRoom(wardId)
                : bedRepository.findAllWithActiveWardAndRoomOrderByWardNameAndBedNumber();
        return beds.stream()
                .map(bed -> {
                    BedResponseDto dto = toDto(bed);
                    dto.setAvailable(BedStatus.AVAILABLE.equals(bed.getBedStatus()) && Boolean.TRUE.equals(bed.getIsActive()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public BedResponseDto updateStatus(Long bedId, BedStatusRequestDto request) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found: " + bedId));
        bed.setBedStatus(request.getBedStatus());
        bed = bedRepository.save(bed);
        BedResponseDto dto = toDto(bed);
        dto.setAvailable(BedStatus.AVAILABLE.equals(bed.getBedStatus()) && Boolean.TRUE.equals(bed.getIsActive()));
        return dto;
    }

    public Bed getEntityById(Long id) {
        return bedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found: " + id));
    }

    public void setBedStatusOccupied(Long bedId) {
        Bed bed = bedRepository.findById(bedId).orElse(null);
        if (bed != null) {
            bed.setBedStatus(BedStatus.OCCUPIED);
            bedRepository.save(bed);
        }
    }

    public void setBedStatusAvailable(Long bedId) {
        Bed bed = bedRepository.findById(bedId).orElse(null);
        if (bed != null) {
            bed.setBedStatus(BedStatus.AVAILABLE);
            bedRepository.save(bed);
        }
    }

    /** Set bed to RESERVED (e.g. on IPD admission submit). */
    public void setBedStatusReserved(Long bedId) {
        Bed bed = bedRepository.findById(bedId).orElse(null);
        if (bed != null) {
            bed.setBedStatus(BedStatus.RESERVED);
            bedRepository.save(bed);
        }
    }

    private BedResponseDto toDto(Bed b) {
        BedResponseDto dto = new BedResponseDto();
        dto.setId(b.getId());
        dto.setWardId(b.getWard().getId());
        dto.setWardName(b.getWard().getName());
        dto.setWardCode(b.getWard().getCode());
        dto.setWardType(b.getWard().getWardType());
        dto.setBedNumber(b.getBedNumber());
        dto.setBedStatus(b.getBedStatus());
        dto.setIsIsolation(b.getIsIsolation());
        dto.setEquipmentReady(b.getEquipmentReady());
        dto.setIsActive(b.getIsActive());
        dto.setCreatedAt(b.getCreatedAt());
        dto.setUpdatedAt(b.getUpdatedAt());
        if (b.getRoom() != null) {
            dto.setRoomId(b.getRoom().getId());
            dto.setRoomNumber(b.getRoom().getRoomNumber());
        }
        return dto;
    }
}
