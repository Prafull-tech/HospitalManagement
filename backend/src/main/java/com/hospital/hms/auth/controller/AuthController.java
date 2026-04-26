package com.hospital.hms.auth.controller;

import com.hospital.hms.auth.entity.AppUser;
import com.hospital.hms.auth.entity.UserRole;
import com.hospital.hms.auth.JwtUtil;
import com.hospital.hms.auth.repository.AppUserRepository;
import com.hospital.hms.common.dto.ApiResponse;
import com.hospital.hms.common.exception.TenantNotFoundException;
import com.hospital.hms.common.logging.MdcKeys;
import com.hospital.hms.hospital.entity.Hospital;
import com.hospital.hms.hospital.repository.HospitalRepository;
import com.hospital.hms.hospital.service.HospitalService;
import com.hospital.hms.config.tenancy.TenantContext;
import com.hospital.hms.tenant.entity.TenantUser;
import com.hospital.hms.tenant.repository.TenantUserRepository;
import com.hospital.hms.tenant.service.TenantResolutionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AppUserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final TenantUserRepository tenantUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final HospitalService hospitalService;
    private final TenantResolutionService tenantResolutionService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          AppUserRepository userRepository,
                          HospitalRepository hospitalRepository,
                          TenantUserRepository tenantUserRepository,
                          PasswordEncoder passwordEncoder,
                          HospitalService hospitalService,
                          TenantResolutionService tenantResolutionService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.tenantUserRepository = tenantUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.hospitalService = hospitalService;
        this.tenantResolutionService = tenantResolutionService;
    }

    // ------------------------------------------------------------------ inner request/response types

    public static class RegisterRequest {
        @NotBlank private String username;
        @NotBlank private String fullName;
        @NotBlank private String password;
        @NotBlank private String role;
        @Positive private Long hospitalId;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public Long getHospitalId() { return hospitalId; }
        public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }
    }

    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class SuperAdminLoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class HospitalLoginRequest {
        @NotBlank private String email;
        @NotBlank private String password;
        private String hospitalSlug;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getHospitalSlug() { return hospitalSlug; }
        public void setHospitalSlug(String hospitalSlug) { this.hospitalSlug = hospitalSlug; }
    }

    public static class ChangePasswordRequest {
        @NotBlank private String currentPassword;
        @NotBlank @Size(min = 8, max = 128) private String newPassword;
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class TemporaryPasswordChangeRequest {
        @NotBlank @Size(min = 8, max = 128) private String newPassword;
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class RefreshRequest {
        @NotBlank private String refreshToken;
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class UpdateProfileRequest {
        @NotBlank @Size(max = 255) private String fullName;
        @Size(max = 100) private String email;
        @Size(max = 20) private String phone;
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class SignupRequest {
        @NotBlank @Size(max = 255) private String fullName;
        @NotBlank @Size(max = 50) private String username;
        @NotBlank @Size(min = 8, max = 128) private String password;
        @Size(max = 100) private String email;
        @Size(max = 20) private String phone;
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    // ------------------------------------------------------------------ helpers

    private Map<String, Object> buildProfileResponse(AppUser user) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("username", user.getUsername());
        m.put("fullName", user.getFullName());
        m.put("role", user.getRole().name());
        m.put("email", user.getEmail() != null ? user.getEmail() : "");
        m.put("phone", user.getPhone() != null ? user.getPhone() : "");
        m.put("active", user.getActive());
        m.put("mustChangePassword", user.getMustChangePassword());
        m.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
        Hospital hospital = user.getHospital();
        m.put("hospitalId", hospital != null ? hospital.getId() : null);
        m.put("hospitalCode", hospital != null ? hospital.getHospitalCode() : "");
        m.put("hospitalName", hospital != null ? hospital.getHospitalName() : "");
        m.put("tenantSlug", hospital != null ? hospital.getSubdomain() : "");
        return m;
    }

    // ------------------------------------------------------------------ endpoints

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        MDC.put(MdcKeys.USER_ID, username);
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);
            AppUser user = userRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            ResponseEntity<?> tenantMismatch = validateTenantHost(user, httpRequest);
            if (tenantMismatch != null) {
                return tenantMismatch;
            }
            Instant issuedAt = Instant.now();
            String accessToken = user.getHospital() == null
                    ? jwtUtil.generateSuperAdminToken(user, issuedAt)
                    : jwtUtil.generateHospitalUserToken(
                            String.valueOf(user.getId()),
                            user.getEmail(),
                            user.getRole().name(),
                            user.getHospital() != null ? user.getHospital().getId() : null,
                            user.getHospital() != null ? user.getHospital().getTenantDbName() : null,
                            issuedAt
                    );
            log.info("Login success for user={}", user.getUsername());
            Map<String, Object> resp = new LinkedHashMap<>(buildProfileResponse(user));
            resp.put("token", accessToken);
            resp.put("issuedAt", issuedAt.toString());
            return ResponseEntity.ok(resp);
        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            log.warn("Login failed for user={}: {}", username, ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Invalid username or password"));
        } finally {
            MDC.remove(MdcKeys.USER_ID);
        }
    }

    @PostMapping("/super-admin/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> superAdminLogin(
            @RequestBody @Validated SuperAdminLoginRequest request) {
        String username = request.getUsername();
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        AppUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (user.getRole() != UserRole.SUPER_ADMIN) {
            throw new BadCredentialsException("Not a super admin");
        }
        Instant issuedAt = Instant.now();
        String token = jwtUtil.generateSuperAdminToken(user, issuedAt);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", user.getFullName());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole().name());
        data.put("user", userMap);
        data.put("hospital", null);
        return ResponseEntity.ok(ApiResponse.success(data, "Login success"));
    }

    @PostMapping("/hospital/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> hospitalLogin(
            @RequestBody @Validated HospitalLoginRequest request,
            HttpServletRequest httpRequest) {
        String slug = request.getHospitalSlug() != null ? request.getHospitalSlug().trim().toLowerCase() : "";
        Hospital hospital;
        if (!slug.isBlank()) {
            hospital = hospitalRepository.findBySubdomainAndDeletedFalse(slug)
                    .orElseThrow(() -> new TenantNotFoundException("Hospital not found"));
        } else {
            hospital = tenantResolutionService.resolveTenantHospital(httpRequest)
                    .orElseThrow(() -> new TenantNotFoundException("Hospital slug is required for hospital login"));
        }
        if (!Boolean.TRUE.equals(hospital.getIsActive())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("This hospital domain is inactive", null));
        }
        String tenantDbName = hospital.getTenantDbName();
        if (tenantDbName == null || tenantDbName.isBlank()) {
            throw new IllegalStateException("Hospital is missing tenantDbName");
        }

        try {
            TenantContext.setCurrentTenant(tenantDbName);
            TenantUser user = tenantUserRepository.findByEmailIgnoreCase(request.getEmail().trim())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
            if (!Boolean.TRUE.equals(user.getActive())) {
                throw new BadCredentialsException("Invalid credentials");
            }
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                throw new BadCredentialsException("Invalid credentials");
            }
            if (user.getId() == null || user.getId().isBlank()) {
                user.setId(UUID.randomUUID().toString());
            }
            Instant issuedAt = Instant.now();
            String token = jwtUtil.generateHospitalUserToken(
                    user.getId(),
                    user.getEmail(),
                    user.getRole(),
                    hospital.getId(),
                    tenantDbName,
                    issuedAt
            );
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("token", token);
            Map<String, Object> userMap = new LinkedHashMap<>();
            userMap.put("id", user.getId());
            userMap.put("name", user.getName());
            userMap.put("email", user.getEmail());
            userMap.put("role", user.getRole());
            data.put("user", userMap);
            Map<String, Object> hospitalMap = new LinkedHashMap<>();
            hospitalMap.put("id", hospital.getId());
            hospitalMap.put("name", hospital.getHospitalName());
            hospitalMap.put("logoUrl", hospital.getLogoUrl());
            hospitalMap.put("slug", hospital.getSubdomain());
            data.put("hospital", hospitalMap);
            return ResponseEntity.ok(ApiResponse.success(data, "Login success"));
        } finally {
            TenantContext.clear();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody @Validated RefreshRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of("status", 410, "message", "Refresh tokens are not supported in the multi-tenant auth flow"));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equalsIgnoreCase(String.valueOf(auth.getPrincipal()))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Not authenticated"));
        }
        AppUser user = userRepository.findByUsernameIgnoreCase(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + auth.getName()));
        return ResponseEntity.ok(buildProfileResponse(user));
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(@RequestBody @Validated UpdateProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Not authenticated"));
        }
        AppUser user = userRepository.findByUsernameIgnoreCase(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + auth.getName()));
        user.setFullName(request.getFullName().trim());
        if (request.getEmail() != null) user.setEmail(request.getEmail().trim());
        if (request.getPhone() != null) user.setPhone(request.getPhone().trim());
        userRepository.save(user);
        log.info("Profile updated for user={}", user.getUsername());
        return ResponseEntity.ok(buildProfileResponse(user));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> register(@RequestBody @Validated RegisterRequest request) {
        if (userRepository.findByUsernameIgnoreCase(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("status", 409, "message", "Username already exists"));
        }
        if (request.getHospitalId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", 400, "message", "hospitalId is required"));
        }
        AppUser user = new AppUser();
        user.setUsername(request.getUsername().trim());
        user.setFullName(request.getFullName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        try {
            user.setRole(UserRole.valueOf(request.getRole()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", 400, "message", "Invalid role: " + request.getRole()));
        }
        user.setHospital(hospitalService.getEntityById(request.getHospitalId()));
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Validated SignupRequest request) {
        if (userRepository.findByUsernameIgnoreCase(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("status", 409, "message", "Username already exists"));
        }
        AppUser user = new AppUser();
        user.setUsername(request.getUsername().trim());
        user.setFullName(request.getFullName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.RECEPTIONIST); // Default role; admin can change later
        if (request.getEmail() != null) user.setEmail(request.getEmail().trim());
        if (request.getPhone() != null) user.setPhone(request.getPhone().trim());
        user.setActive(false); // Inactive until admin approves
        userRepository.save(user);
        log.info("Self-registration (inactive) for user={}", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Account created. An administrator will activate your account."));
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@RequestBody @Validated ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Not authenticated"));
        }
        String username = auth.getName();
        AppUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", 400, "message", "Current password is incorrect"));
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", 400, "message", "New password must differ from the current password"));
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
        log.info("Password changed for user={}", username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-temporary-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changeTemporaryPassword(@RequestBody @Validated TemporaryPasswordChangeRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Not authenticated"));
        }
        String username = auth.getName();
        AppUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (!Boolean.TRUE.equals(user.getMustChangePassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", 400, "message", "Temporary password change is not required for this account"));
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", 400, "message", "New password must differ from the temporary password"));
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
        log.info("Temporary password replaced for user={}", username);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> validateTenantHost(AppUser user, HttpServletRequest request) {
        return tenantResolutionService.resolveTenantHospital(request)
                .map(hospital -> {
                    if (!Boolean.TRUE.equals(hospital.getIsActive())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("status", 403, "message", "This hospital domain is inactive"));
                    }
                    if (user.getHospital() == null || !hospital.getId().equals(user.getHospital().getId())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("status", 401, "message", "This account is not valid for the current hospital domain"));
                    }
                    return null;
                })
                .orElse(null);
    }
}
