package com.hospital.hms.walkin.controller;

import com.hospital.hms.token.service.TokenService;
import com.hospital.hms.walkin.dto.WalkInRegisterRequestDto;
import com.hospital.hms.walkin.dto.WalkInRegisterResponseDto;
import com.hospital.hms.walkin.service.WalkInService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Walk-in registration API. Base path: /api (context) + /walkin (mapping).
 */
@RestController
@RequestMapping("/walkin")
public class WalkInController {

    private final WalkInService walkInService;
    private final TokenService tokenService;

    public WalkInController(WalkInService walkInService, TokenService tokenService) {
        this.walkInService = walkInService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<WalkInRegisterResponseDto> register(@Valid @RequestBody WalkInRegisterRequestDto request) {
        WalkInRegisterResponseDto result = walkInService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Long>> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(tokenService.getWalkInStats(date));
    }
}
