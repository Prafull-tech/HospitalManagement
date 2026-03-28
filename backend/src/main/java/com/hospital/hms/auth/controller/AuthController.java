package com.hospital.hms.auth.controller;

import com.hospital.hms.auth.entity.AppUser;
import com.hospital.hms.auth.entity.RefreshToken;
import com.hospital.hms.auth.entity.UserRole;
import com.hospital.hms.auth.jwt.JwtTokenService;
import com.hospital.hms.auth.repository.AppUserRepository;
import com.hospital.hms.common.logging.MdcKeys;
import jakarta.validation.constraints.NotBlank;
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

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService tokenService;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenService tokenService,
                          AppUserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------------------------------------------------------------ inner request/response types

    public static class RegisterRequest {
        @NotBlank private String username;
        @NotBlank private String fullName;
        @NotBlank private String password;
        @NotBlank private String role;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ChangePasswordRequest {
        @NotBlank private String currentPassword;
        @NotBlank @Size(min = 8, max = 128) private String newPassword;
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
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

    // ------------------------------------------------------------------ helpers

    private Map<String, Object> buildProfileResponse(AppUser user) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("username", user.getUsername());
        m.put("fullName", user.getFullName());
        m.put("role", user.getRole().name());
        m.put("email", user.getEmail() != null ? user.getEmail() : "");
        m.put("phone", user.getPhone() != null ? user.getPhone() : "");
        m.put("active", user.getActive());
        m.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
        return m;
    }

    // ------------------------------------------------------------------ endpoints

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated LoginRequest request) {
        String username = request.getUsername();
        MDC.put(MdcKeys.USER_ID, username);
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);
            AppUser user = userRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            String accessToken = tokenService.generateToken(user.getUsername(), user.getRole());
            String refreshToken = tokenService.createRefreshToken(user.getUsername());
            log.info("Login success for user={}", user.getUsername());
            Map<String, Object> resp = new LinkedHashMap<>(buildProfileResponse(user));
            resp.put("token", accessToken);
            resp.put("refreshToken", refreshToken);
            resp.put("issuedAt", Instant.now().toString());
            return ResponseEntity.ok(resp);
        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            log.warn("Login failed for user={}: {}", username, ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Invalid username or password"));
        } finally {
            MDC.remove(MdcKeys.USER_ID);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody @Validated RefreshRequest request) {
        RefreshToken existing = tokenService.validateRefreshToken(request.getRefreshToken());
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Invalid or expired refresh token"));
        }
        AppUser user = userRepository.findByUsernameIgnoreCase(existing.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String newAccessToken = tokenService.generateToken(user.getUsername(), user.getRole());
        String newRefreshToken = tokenService.rotateRefreshToken(existing);
        log.info("Token refreshed for user={}", user.getUsername());
        Map<String, Object> resp = new LinkedHashMap<>(buildProfileResponse(user));
        resp.put("token", newAccessToken);
        resp.put("refreshToken", newRefreshToken);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            tokenService.revokeAllTokensForUser(auth.getName());
            log.info("Logout (all refresh tokens revoked) for user={}", auth.getName());
        }
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> register(@RequestBody @Validated RegisterRequest request) {
        if (userRepository.findByUsernameIgnoreCase(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("status", 409, "message", "Username already exists"));
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
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
        userRepository.save(user);
        tokenService.revokeAllTokensForUser(username);
        log.info("Password changed for user={}", username);
        return ResponseEntity.noContent().build();
    }
}
