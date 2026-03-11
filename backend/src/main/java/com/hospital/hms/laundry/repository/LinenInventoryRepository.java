package com.hospital.hms.laundry.repository;

import com.hospital.hms.laundry.entity.LinenInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinenInventoryRepository extends JpaRepository<LinenInventory, Long> {

    List<LinenInventory> findByWardNameOrderByCreatedAtDesc(String wardName);

    List<LinenInventory> findAllByOrderByCreatedAtDesc();
}
