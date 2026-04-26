package com.hospital.hms.auth.controller;

import com.hospital.hms.auth.service.HospitalAdminUserService;
import com.hospital.hms.superadmin.dto.CreateHospitalUserRequest;
import com.hospital.hms.superadmin.dto.HospitalUserDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class HospitalAdminUserController {

    private final HospitalAdminUserService hospitalAdminUserService;

    public HospitalAdminUserController(HospitalAdminUserService hospitalAdminUserService) {
        this.hospitalAdminUserService = hospitalAdminUserService;
    }

    @GetMapping
    public ResponseEntity<List<HospitalUserDto>> listCurrentHospitalUsers() {
        return ResponseEntity.ok(hospitalAdminUserService.listCurrentHospitalUsers());
    }

    @PostMapping
    public ResponseEntity<HospitalUserDto> createCurrentHospitalUser(@Valid @RequestBody CreateHospitalUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hospitalAdminUserService.createCurrentHospitalUser(request));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<Void> updateCurrentHospitalUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> body) {
        Boolean active = body.get("active");
        if (active == null) {
            return ResponseEntity.badRequest().build();
        }
        hospitalAdminUserService.updateCurrentHospitalUserStatus(userId, active);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<Void> resetCurrentHospitalUserPassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body
    ) {
        String temporaryPassword = body.get("temporaryPassword");
        if (temporaryPassword == null || temporaryPassword.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        hospitalAdminUserService.resetCurrentHospitalUserPassword(userId, temporaryPassword);
        return ResponseEntity.noContent().build();
    }
}