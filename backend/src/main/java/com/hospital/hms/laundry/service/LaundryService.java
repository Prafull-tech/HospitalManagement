package com.hospital.hms.laundry.service;

import com.hospital.hms.common.logging.SecurityContextUserResolver;
import com.hospital.hms.laundry.dto.LaundryIssueRequestDto;
import com.hospital.hms.laundry.dto.LaundryReturnRequestDto;
import com.hospital.hms.laundry.dto.LinenInventoryResponseDto;
import com.hospital.hms.laundry.entity.LinenInventory;
import com.hospital.hms.laundry.entity.LaundryStatus;
import com.hospital.hms.laundry.repository.LinenInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Laundry service: issue linen to ward, return from ward, get status.
 */
@Service
public class LaundryService {

    private static final Logger log = LoggerFactory.getLogger(LaundryService.class);

    private final LinenInventoryRepository inventoryRepository;

    public LaundryService(LinenInventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public LinenInventoryResponseDto issue(LaundryIssueRequestDto request) {
        LinenInventory inv = new LinenInventory();
        inv.setWardName(request.getWardName());
        inv.setLinenType(request.getLinenType());
        inv.setQuantityIssued(request.getQuantity());
        inv.setQuantityReturned(0);
        inv.setLaundryStatus(LaundryStatus.DIRTY);
        inv.setIpdAdmissionId(request.getIpdAdmissionId());

        inv = inventoryRepository.save(inv);
        String user = SecurityContextUserResolver.resolveUserId();
        log.info("Laundry issued ward={} type={} qty={} id={} by {}",
                inv.getWardName(), inv.getLinenType(), inv.getQuantityIssued(), inv.getId(), user);
        return toDto(inv);
    }

    @Transactional
    public LinenInventoryResponseDto returnLinen(LaundryReturnRequestDto request) {
        LinenInventory inv = new LinenInventory();
        inv.setWardName(request.getWardName());
        inv.setLinenType(request.getLinenType());
        inv.setQuantityIssued(0);
        inv.setQuantityReturned(request.getQuantity());
        inv.setLaundryStatus(LaundryStatus.DIRTY);
        inv.setIpdAdmissionId(request.getIpdAdmissionId());

        inv = inventoryRepository.save(inv);
        String user = SecurityContextUserResolver.resolveUserId();
        log.info("Laundry returned ward={} type={} qty={} id={} by {}",
                inv.getWardName(), inv.getLinenType(), inv.getQuantityReturned(), inv.getId(), user);
        return toDto(inv);
    }

    @Transactional(readOnly = true)
    public List<LinenInventoryResponseDto> getStatus(String wardName) {
        List<LinenInventory> list = wardName != null && !wardName.isBlank()
                ? inventoryRepository.findByWardNameOrderByCreatedAtDesc(wardName)
                : inventoryRepository.findAllByOrderByCreatedAtDesc();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    private LinenInventoryResponseDto toDto(LinenInventory inv) {
        LinenInventoryResponseDto dto = new LinenInventoryResponseDto();
        dto.setId(inv.getId());
        dto.setLinenType(inv.getLinenType());
        dto.setWardName(inv.getWardName());
        dto.setQuantityIssued(inv.getQuantityIssued());
        dto.setQuantityReturned(inv.getQuantityReturned());
        dto.setLaundryStatus(inv.getLaundryStatus());
        dto.setIpdAdmissionId(inv.getIpdAdmissionId());
        dto.setCreatedAt(inv.getCreatedAt());
        dto.setUpdatedAt(inv.getUpdatedAt());
        return dto;
    }
}
