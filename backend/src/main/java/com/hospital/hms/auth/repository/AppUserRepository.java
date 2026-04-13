package com.hospital.hms.auth.repository;

import com.hospital.hms.auth.entity.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    @EntityGraph(attributePaths = "hospital")
    Optional<AppUser> findByUsernameIgnoreCase(String username);

    List<AppUser> findByHospitalId(Long hospitalId);
}

