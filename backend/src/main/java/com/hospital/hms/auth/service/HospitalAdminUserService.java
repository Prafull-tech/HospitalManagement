package com.hospital.hms.auth.service;

import com.hospital.hms.auth.entity.AppUser;
import com.hospital.hms.auth.entity.UserRole;
import com.hospital.hms.auth.repository.AppUserRepository;
import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.superadmin.dto.CreateHospitalUserRequest;
import com.hospital.hms.superadmin.dto.HospitalUserDto;
import com.hospital.hms.tenant.service.TenantContextService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalAdminUserService {

    private final TenantContextService tenantContextService;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public HospitalAdminUserService(TenantContextService tenantContextService,
                                    AppUserRepository appUserRepository,
                                    PasswordEncoder passwordEncoder) {
        this.tenantContextService = tenantContextService;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<HospitalUserDto> listCurrentHospitalUsers() {
        Hospital hospital = tenantContextService.requireCurrentHospital();
        return appUserRepository.findByHospitalId(hospital.getId())
                .stream()
                .map(user -> toUserDto(user, hospital))
                .collect(Collectors.toList());
    }

    @Transactional
    public HospitalUserDto createCurrentHospitalUser(CreateHospitalUserRequest request) {
        Hospital hospital = tenantContextService.requireCurrentHospital();
        String trimmedUsername = request.getUsername().trim();
        if (appUserRepository.findByUsernameIgnoreCase(trimmedUsername).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }
        if (role == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("SUPER_ADMIN role cannot be assigned inside a hospital workspace");
        }

        AppUser user = new AppUser();
        user.setUsername(trimmedUsername);
        user.setFullName(request.getFullName().trim());
        // Avoid accidental leading/trailing whitespace in temp passwords.
        user.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
        user.setRole(role);
        user.setHospital(hospital);
        user.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setActive(true);
        user.setMustChangePassword(true);
        user = appUserRepository.save(user);
        return toUserDto(user, hospital);
    }

    @Transactional
    public void resetCurrentHospitalUserPassword(long userId, String temporaryPassword) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Hospital userHospital = user.getHospital();
        if (userHospital == null || userHospital.getId() == null || !hospitalId.equals(userHospital.getId())) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword.trim()));
        user.setMustChangePassword(true);
        appUserRepository.save(user);
    }

    @Transactional
    public void updateCurrentHospitalUserStatus(long userId, boolean active) {
        Long hospitalId = tenantContextService.requireCurrentHospitalId();
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Hospital userHospital = user.getHospital();
        if (userHospital == null || userHospital.getId() == null || !hospitalId.equals(userHospital.getId())) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        user.setActive(active);
        appUserRepository.save(user);
    }

    private HospitalUserDto toUserDto(AppUser user, Hospital hospital) {
        HospitalUserDto dto = new HospitalUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole().name());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setActive(user.getActive());
        dto.setMustChangePassword(user.getMustChangePassword());
        dto.setHospitalId(hospital.getId());
        dto.setHospitalName(hospital.getHospitalName());
        return dto;
    }
}