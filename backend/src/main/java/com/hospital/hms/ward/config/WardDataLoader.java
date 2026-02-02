package com.hospital.hms.ward.config;

import com.hospital.hms.ward.entity.Bed;
import com.hospital.hms.ward.entity.BedStatus;
import com.hospital.hms.ward.entity.Room;
import com.hospital.hms.ward.entity.Ward;
import com.hospital.hms.ward.entity.WardType;
import com.hospital.hms.ward.repository.BedRepository;
import com.hospital.hms.ward.repository.RoomRepository;
import com.hospital.hms.ward.repository.WardRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Seeds sample wards, rooms, and beds when none exist (dev only). Safe for H2 and MySQL.
 */
@Configuration
public class WardDataLoader {

    @Bean
    @Order(5)
    public ApplicationRunner seedWardsRoomsAndBeds(WardRepository wardRepo,
                                                   RoomRepository roomRepo,
                                                   BedRepository bedRepo) {
        return args -> {
            if (wardRepo.count() > 0) return;
            // General Ward: 20 beds to match Hospital Bed Availability config (GENERAL totalBeds = 20)
            Ward w1 = new Ward();
            w1.setCode("GW-1");
            w1.setName("General Ward 1");
            w1.setWardType(WardType.GENERAL);
            w1.setCapacity(20);
            w1.setChargeCategory("GENERAL");
            w1.setIsActive(true);
            w1 = wardRepo.save(w1);
            Room r1 = new Room();
            r1.setWard(w1);
            r1.setRoomNumber("R1");
            r1.setIsActive(true);
            r1 = roomRepo.save(r1);
            Room r2 = new Room();
            r2.setWard(w1);
            r2.setRoomNumber("R2");
            r2.setIsActive(true);
            r2 = roomRepo.save(r2);
            for (int i = 1; i <= 10; i++) {
                Bed b = new Bed();
                b.setWard(w1);
                b.setRoom(r1);
                b.setBedNumber("B" + i);
                b.setBedStatus(BedStatus.AVAILABLE);
                b.setIsActive(true);
                bedRepo.save(b);
            }
            for (int i = 11; i <= 20; i++) {
                Bed b = new Bed();
                b.setWard(w1);
                b.setRoom(r2);
                b.setBedNumber("B" + i);
                b.setBedStatus(BedStatus.AVAILABLE);
                b.setIsActive(true);
                bedRepo.save(b);
            }
            Ward w2 = new Ward();
            w2.setCode("ICU-1");
            w2.setName("ICU 1");
            w2.setWardType(WardType.ICU);
            w2.setCapacity(4);
            w2.setChargeCategory("ICU");
            w2.setIsActive(true);
            w2 = wardRepo.save(w2);
            for (int i = 1; i <= 4; i++) {
                Bed b = new Bed();
                b.setWard(w2);
                b.setBedNumber("ICU-" + i);
                b.setBedStatus(BedStatus.AVAILABLE);
                b.setEquipmentReady(true);
                b.setIsActive(true);
                bedRepo.save(b);
            }
            Ward w3 = new Ward();
            w3.setCode("PVT-1");
            w3.setName("Private Ward 1");
            w3.setWardType(WardType.PRIVATE);
            w3.setCapacity(3);
            w3.setChargeCategory("PRIVATE");
            w3.setIsActive(true);
            w3 = wardRepo.save(w3);
            for (int i = 1; i <= 3; i++) {
                Bed b = new Bed();
                b.setWard(w3);
                b.setBedNumber("P" + i);
                b.setBedStatus(BedStatus.AVAILABLE);
                b.setIsActive(true);
                bedRepo.save(b);
            }
        };
    }
}
