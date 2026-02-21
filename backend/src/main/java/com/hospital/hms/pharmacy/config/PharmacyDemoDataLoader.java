package com.hospital.hms.pharmacy.config;

import com.hospital.hms.doctor.entity.Doctor;
import com.hospital.hms.doctor.entity.DoctorStatus;
import com.hospital.hms.doctor.entity.DoctorType;
import com.hospital.hms.doctor.entity.MedicalDepartment;
import com.hospital.hms.doctor.repository.DoctorRepository;
import com.hospital.hms.doctor.repository.MedicalDepartmentRepository;
import com.hospital.hms.ipd.entity.AdmissionStatus;
import com.hospital.hms.ipd.entity.AdmissionType;
import com.hospital.hms.ipd.entity.BedAllocation;
import com.hospital.hms.ipd.entity.IPDAdmission;
import com.hospital.hms.ipd.repository.BedAllocationRepository;
import com.hospital.hms.ipd.repository.IPDAdmissionRepository;
import com.hospital.hms.ipd.service.IPDAdmissionNumberGenerator;
import com.hospital.hms.pharmacy.entity.MedicineCategory;
import com.hospital.hms.pharmacy.entity.MedicineForm;
import com.hospital.hms.pharmacy.entity.MedicineMaster;
import com.hospital.hms.pharmacy.entity.StorageType;
import com.hospital.hms.pharmacy.repository.MedicineMasterRepository;
import com.hospital.hms.reception.entity.Patient;
import com.hospital.hms.reception.repository.PatientRepository;
import com.hospital.hms.ward.entity.Bed;
import com.hospital.hms.ward.entity.BedStatus;
import com.hospital.hms.ward.entity.Ward;
import com.hospital.hms.ward.repository.BedRepository;
import com.hospital.hms.ward.repository.WardRepository;
import com.hospital.hms.ward.service.BedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds demo data for Pharmacy Issue Queue: patients, doctors, IPD admissions, medicines.
 * Runs only in dev profile. MedicationOrderDataLoader (Order 50) creates orders from this data.
 */
@Configuration
@Profile("dev")
public class PharmacyDemoDataLoader {

    private static final Logger log = LoggerFactory.getLogger(PharmacyDemoDataLoader.class);

    @Bean
    @Order(8)
    public ApplicationRunner seedPharmacyDemoData(
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            MedicalDepartmentRepository departmentRepository,
            WardRepository wardRepository,
            BedRepository bedRepository,
            IPDAdmissionRepository admissionRepository,
            BedAllocationRepository bedAllocationRepository,
            IPDAdmissionNumberGenerator admissionNumberGenerator,
            BedService bedService,
            MedicineMasterRepository medicineRepository) {
        return args -> {
            if (admissionRepository.countByAdmissionStatusIn(
                    List.of(AdmissionStatus.ADMITTED, AdmissionStatus.ACTIVE)) > 0) {
                log.debug("Pharmacy demo: IPD admissions already exist, skipping IPD seed");
                seedMedicinesOnly(medicineRepository);
                return;
            }

            // 1. Patients
            Patient p1 = patientRepository.findByUhid("DEMO-UHID-001").orElseGet(() -> {
                Patient p = new Patient();
                p.setUhid("DEMO-UHID-001");
                p.setRegistrationNumber("REG-" + System.currentTimeMillis());
                p.setRegistrationDate(LocalDateTime.now());
                p.setFullName("Demo Patient One");
                p.setAge(45);
                p.setGender("M");
                p.setPhone("9876543210");
                p = patientRepository.save(p);
                log.info("Pharmacy demo: seeded patient {}", p.getUhid());
                return p;
            });

            Patient p2 = patientRepository.findByUhid("DEMO-UHID-002").orElseGet(() -> {
                Patient p = new Patient();
                p.setUhid("DEMO-UHID-002");
                p.setRegistrationNumber("REG-" + (System.currentTimeMillis() + 1));
                p.setRegistrationDate(LocalDateTime.now());
                p.setFullName("Demo Patient Two");
                p.setAge(62);
                p.setGender("F");
                p.setPhone("9876543211");
                p = patientRepository.save(p);
                log.info("Pharmacy demo: seeded patient {}", p.getUhid());
                return p;
            });

            // 2. Doctors
            MedicalDepartment dept = departmentRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("No departments. Run DoctorDataLoader first."));
            Doctor doctor = doctorRepository.findAll().stream().findFirst().orElseGet(() -> {
                Doctor d = new Doctor();
                d.setCode("DOC-DEMO-001");
                d.setFullName("Dr. Demo Consultant");
                d.setDepartment(dept);
                d.setDoctorType(DoctorType.CONSULTANT);
                d.setStatus(DoctorStatus.ACTIVE);
                d = doctorRepository.save(d);
                log.info("Pharmacy demo: seeded doctor {}", d.getCode());
                return d;
            });

            // 3. Beds (first available from General Ward)
            Ward ward = wardRepository.findByCode("GW-1")
                    .orElse(wardRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new IllegalStateException("No wards. Run WardDataLoader first.")));
            List<Bed> availableBeds = bedRepository.findByWardIdAndBedStatusAndIsActiveTrue(ward.getId(), BedStatus.AVAILABLE);
            if (availableBeds.size() < 2) {
                log.warn("Pharmacy demo: need at least 2 available beds, found {}. Skipping IPD seed.", availableBeds.size());
                seedMedicinesOnly(medicineRepository);
                return;
            }

            Bed bed1 = availableBeds.get(0);
            Bed bed2 = availableBeds.get(1);

            // 4. IPD Admissions with bed allocations
            IPDAdmission adm1 = new IPDAdmission();
            adm1.setAdmissionNumber(admissionNumberGenerator.generate());
            adm1.setPatient(p1);
            adm1.setPrimaryDoctor(doctor);
            adm1.setAdmissionType(AdmissionType.OPD_REFERRAL);
            adm1.setAdmissionStatus(AdmissionStatus.ACTIVE);
            adm1.setAdmissionDateTime(LocalDateTime.now().minusDays(2));
            adm1.setDiagnosis("Fever, URTI");
            adm1 = admissionRepository.save(adm1);

            BedAllocation alloc1 = new BedAllocation();
            alloc1.setBed(bed1);
            alloc1.setAdmission(adm1);
            alloc1.setAllocatedAt(Instant.now());
            bedAllocationRepository.save(alloc1);
            bedService.setBedStatusOccupied(bed1.getId());

            IPDAdmission adm2 = new IPDAdmission();
            adm2.setAdmissionNumber(admissionNumberGenerator.generate());
            adm2.setPatient(p2);
            adm2.setPrimaryDoctor(doctor);
            adm2.setAdmissionType(AdmissionType.EMERGENCY);
            adm2.setAdmissionStatus(AdmissionStatus.ACTIVE);
            adm2.setAdmissionDateTime(LocalDateTime.now().minusDays(1));
            adm2.setDiagnosis("Hypertension");
            adm2 = admissionRepository.save(adm2);

            BedAllocation alloc2 = new BedAllocation();
            alloc2.setBed(bed2);
            alloc2.setAdmission(adm2);
            alloc2.setAllocatedAt(Instant.now());
            bedAllocationRepository.save(alloc2);
            bedService.setBedStatusOccupied(bed2.getId());

            log.info("Pharmacy demo: seeded 2 IPD admissions");

            // 5. Medicines
            seedMedicinesOnly(medicineRepository);
        };
    }

    private void seedMedicinesOnly(MedicineMasterRepository medicineRepository) {
        if (medicineRepository.count() > 0) {
            log.debug("Pharmacy demo: medicines already exist, skipping");
            return;
        }
        String[][] meds = {
                { "CEFTRIAXONE_1G", "Ceftriaxone 1g IV", "ANTIBIOTIC", "1g", "IV", "50", "ROOM_TEMP" },
                { "PARACETAMOL_500", "Paracetamol 500mg", "ANALGESIC", "500mg", "TABLET", "200", "ROOM_TEMP" },
                { "AMOXICILLIN_500", "Amoxicillin 500mg", "ANTIBIOTIC", "500mg", "CAPSULE", "100", "ROOM_TEMP" },
                { "METFORMIN_500", "Metformin 500mg", "DIABETIC", "500mg", "TABLET", "150", "ROOM_TEMP" },
                { "LOSARTAN_50", "Losartan 50mg", "CARDIAC", "50mg", "TABLET", "80", "ROOM_TEMP" },
        };
        for (String[] m : meds) {
            MedicineMaster med = new MedicineMaster();
            med.setMedicineCode(m[0]);
            med.setMedicineName(m[1]);
            med.setCategory(MedicineCategory.valueOf(m[2]));
            med.setStrength(m[3]);
            med.setForm(MedicineForm.valueOf(m[4]));
            med.setMinStock(Integer.parseInt(m[5]));
            med.setQuantity(100);
            med.setLasaFlag(false);
            med.setStorageType(StorageType.valueOf(m[6]));
            med.setActive(true);
            medicineRepository.save(med);
        }
        log.info("Pharmacy demo: seeded {} medicines", meds.length);
    }
}
