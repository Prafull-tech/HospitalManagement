package com.hospital.hms.auth.entity;

import com.hospital.hms.common.entity.BaseIdEntity;
import com.hospital.hms.hospital.entity.Hospital;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Application user for authentication & RBAC.
 * Backed by DB so we can seed dev users and later integrate with HR.
 */
@Entity
@Table(
        name = "hms_users",
        indexes = {
                @Index(name = "idx_user_username", columnList = "username", unique = true),
                @Index(name = "idx_user_role", columnList = "role"),
                @Index(name = "idx_user_hospital_role", columnList = "hospital_id, role")
        }
)
public class AppUser extends BaseIdEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotBlank
    @Size(max = 255)
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

        @NotNull
        @Column(name = "must_change_password", nullable = false)
        private Boolean mustChangePassword = false;

        @NotNull
        @Column(name = "token_version", nullable = false)
        private Long tokenVersion = 0L;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "hospital_id")
        private Hospital hospital;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

        public Boolean getMustChangePassword() { return mustChangePassword; }
        public void setMustChangePassword(Boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }

        public Long getTokenVersion() { return tokenVersion; }
        public void setTokenVersion(Long tokenVersion) { this.tokenVersion = tokenVersion; }

        public Hospital getHospital() { return hospital; }
        public void setHospital(Hospital hospital) { this.hospital = hospital; }
}
