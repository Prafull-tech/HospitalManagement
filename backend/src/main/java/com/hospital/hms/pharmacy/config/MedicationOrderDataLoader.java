package com.hospital.hms.pharmacy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.pharmacy.entity.MedicationOrder;
import com.hospital.hms.pharmacy.entity.MedicationOrderPriority;
import com.hospital.hms.pharmacy.entity.MedicationOrderStatus;
import com.hospital.hms.pharmacy.entity.MedicationOrderWardType;
import com.hospital.hms.pharmacy.entity.MedicineMaster;
import com.hospital.hms.pharmacy.repository.MedicationOrderRepository;
import com.hospital.hms.pharmacy.repository.MedicineMasterRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds sample medication orders for pharmacy issue queue (dev/demo).
 */
@Configuration
public class MedicationOrderDataLoader {

    private static final Logger log = LoggerFactory.getLogger(MedicationOrderDataLoader.class);

    @Bean
    @Order(50)
    public ApplicationRunner seedMedicationOrders(MedicationOrderRepository orderRepository,
                                                      MedicineMasterRepository medicineRepository,
                                                      IPDAdmissionRepository admissionRepository,
                                                      DoctorRepository doctorRepository) {
        return args -> {
            List<MedicineMaster> medicines = medicineRepository.findAll().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getActive())).limit(5).toList();
            List<IPDAdmission> admissions = admissionRepository.findByAdmissionStatusInWithPatient(
                    List.of(AdmissionStatus.ADMITTED, AdmissionStatus.ACTIVE, AdmissionStatus.TRANSFERRED, AdmissionStatus.DISCHARGE_INITIATED))
                    .stream().limit(5).toList();
            List<Doctor> doctors = doctorRepository.findAll().stream().limit(1).toList();

            if (medicines.isEmpty() || admissions.isEmpty() || doctors.isEmpty()) {
                log.debug("MedicationOrder seed skipped: medicines={}, admissions={}, doctors={}",
                        medicines.size(), admissions.size(), doctors.size());
                return;
            }

            List<Long> admissionIdsWithOrders = orderRepository.findByStatusOrderByPriorityAndOrderedAt(MedicationOrderStatus.PENDING)
                    .stream().map(MedicationOrder::getIpdAdmissionId).filter(java.util.Objects::nonNull).distinct().toList();

            Doctor doctor = doctors.get(0);
            int added = 0;
            for (IPDAdmission adm : admissions) {
                if (admissionIdsWithOrders.contains(adm.getId())) continue;
                for (MedicineMaster med : medicines) {
                    if (added >= 6) break;
                    MedicationOrder order = new MedicationOrder();
                    order.setPatient(adm.getPatient());
                    order.setUhid(adm.getPatient().getUhid());
                    order.setIpdAdmissionId(adm.getId());
                    order.setWardType(MedicationOrderWardType.GENERAL);
                    order.setMedicine(med);
                    order.setDosage(med.getStrength());
                    order.setQuantity(2);
                    order.setPriority(MedicationOrderPriority.NORMAL);
                    order.setStatus(MedicationOrderStatus.PENDING);
                    order.setOrderedByDoctor(doctor);
                    order.setOrderedAt(LocalDateTime.now().minusMinutes(15));
                    orderRepository.save(order);
                    added++;
                }
            }
            if (added > 0) {
                log.info("MedicationOrder seed: created {} pending orders for pharmacy issue queue", added);
            }
        };
    }
}
