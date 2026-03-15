package com.hospital.hms.token.controller;

import com.hospital.hms.token.dto.*;
import com.hospital.hms.token.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * OPD Token Management API. Base path: /api (context) + /tokens (mapping).
 */
@RestController
@RequestMapping("/tokens")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/generate")
    public ResponseEntity<TokenResponseDto> generate(@Valid @RequestBody TokenGenerateRequestDto request) {
        TokenResponseDto created = tokenService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/queue/{doctorId}")
    public ResponseEntity<List<TokenResponseDto>> getQueue(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(tokenService.getQueue(doctorId, date));
    }

    @GetMapping("/current")
    public ResponseEntity<List<TokenDisplayDto>> getCurrent(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long doctorId) {
        if (doctorId != null) {
            TokenDisplayDto one = tokenService.getCurrentDisplay(doctorId, date);
            return ResponseEntity.ok(List.of(one));
        }
        return ResponseEntity.ok(tokenService.getAllCurrentDisplays(date));
    }

    @PutMapping("/call-next/{doctorId}")
    public ResponseEntity<TokenResponseDto> callNext(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(tokenService.callNext(doctorId, date));
    }

    @PutMapping("/start/{tokenId}")
    public ResponseEntity<TokenResponseDto> startConsultation(@PathVariable Long tokenId) {
        return ResponseEntity.ok(tokenService.startConsultation(tokenId));
    }

    @PutMapping("/complete/{tokenId}")
    public ResponseEntity<TokenResponseDto> complete(@PathVariable Long tokenId) {
        return ResponseEntity.ok(tokenService.complete(tokenId));
    }

    @PutMapping("/skip/{tokenId}")
    public ResponseEntity<TokenResponseDto> skip(@PathVariable Long tokenId) {
        return ResponseEntity.ok(tokenService.skip(tokenId));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<TokenDashboardDto> getDashboard(
            @RequestParam Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(tokenService.getDashboard(doctorId, date));
    }
}
