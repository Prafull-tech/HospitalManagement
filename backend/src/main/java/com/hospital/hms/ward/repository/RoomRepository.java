package com.hospital.hms.ward.repository;

import com.hospital.hms.ward.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Room repository. DB-agnostic.
 */
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByWardIdAndIsActiveTrueOrderByRoomNumberAsc(Long wardId);

    Optional<Room> findByWardIdAndRoomNumber(Long wardId, String roomNumber);
}
