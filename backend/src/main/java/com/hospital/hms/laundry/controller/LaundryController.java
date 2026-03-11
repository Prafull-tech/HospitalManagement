package com.hospital.hms.laundry.controller;

import com.hospital.hms.laundry.dto.LaundryIssueRequestDto;
import com.hospital.hms.laundry.dto.LaundryReturnRequestDto;
import com.hospital.hms.laundry.dto.LinenInventoryResponseDto;
import com.hospital.hms.laundry.service.LaundryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Laundry API. Base path: /api (context) + /laundry (mapping).
 * <ul>
 *   <li>POST /api/laundry/issue — Issue linen to ward</li>
 *   <li>POST /api/laundry/return — Return linen from ward</li>
 *   <li>GET  /api/laundry/status — Get linen status (optional wardName filter)</li>
 * </ul>
 */
@RestController
@RequestMapping("/laundry")
public class LaundryController {

    private final LaundryService laundryService;

    public LaundryController(LaundryService laundryService) {
        this.laundryService = laundryService;
    }

    @PostMapping("/issue")
    public ResponseEntity<LinenInventoryResponseDto> issue(@Valid @RequestBody LaundryIssueRequestDto request) {
        LinenInventoryResponseDto created = laundryService.issue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/return")
    public ResponseEntity<LinenInventoryResponseDto> returnLinen(@Valid @RequestBody LaundryReturnRequestDto request) {
        LinenInventoryResponseDto created = laundryService.returnLinen(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/status")
    public ResponseEntity<List<LinenInventoryResponseDto>> getStatus(
            @RequestParam(required = false) String wardName) {
        List<LinenInventoryResponseDto> status = laundryService.getStatus(wardName);
        return ResponseEntity.ok(status);
    }
}
